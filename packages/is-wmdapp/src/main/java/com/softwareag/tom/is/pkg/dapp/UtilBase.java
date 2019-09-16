/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.conf.Node;
import com.softwareag.tom.contract.ConfigLocationFileSystem;
import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.contract.ContractRegistry;
import com.softwareag.tom.contract.SolidityLocationFileSystem;
import com.softwareag.tom.protocol.Web3Service;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.ServiceHttp;
import com.softwareag.tom.protocol.util.HexValue;
import rx.Observable;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @param <N> The contract's unique constructor, function, or event representation.
 */
public abstract class UtilBase<N> {
    private ContractRegistry contractRegistry;
    private Map<String, Contract> contracts;
    Web3Service web3Service;

    /**
     * The default constructor.
     * @throws ExceptionInInitializerError if the node configuration is missing
     */
    UtilBase(String nodeName) throws ExceptionInInitializerError {
        try {
            Node node = Node.instance(nodeName);
            URI contractRegistryLocation = node.getContract().getRegistry().getLocationAsUri();
            URI configLocation = node.getConfig().getLocationAsUri();
            contractRegistry = ContractRegistry.build(new SolidityLocationFileSystem(contractRegistryLocation), new ConfigLocationFileSystem(configLocation));
            web3Service = Web3Service.build(new ServiceHttp("http://" + node.getHost().getIp() + ':' + node.getHost().getPort() + "/rpc"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * @param name The contract's constructor, function, or event name
     * @return the contract's URI
     */
    abstract String getContractUri(N name);

    /**
     * @param name The contract's function name
     * @return the contract's function URI
     */
    abstract String getFunctionUri(N name);

    /**
     * @param name The contract's event name
     * @return the contract's event URI
     */
    abstract String getEventUri(N name);

    /**
     * @param name The contract's constructor, function, or event name
     * @return the contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public String deployContract(N name) throws IOException {
        return deployContract(getContractUri(name));
    }

    /**
     * @param uri The contract's local location
     * @return the contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public String deployContract(String uri) throws IOException {
        loadContracts();
        Contract contract = getContract(uri);
        if (contract.getContractAddress() != null) {
            throw new IllegalStateException("Contract address not null; it seems the contract was already deployed!");
        } else {
            sendTransaction(contract, contract.getBinary());
        }
        return contract.getContractAddress();
    }

    /**
     * @param name The contract's constructor, function, or event name
     * @param contractAddress The contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public void storeContractAddress(N name, String contractAddress) throws IOException {
        storeContractAddress(getContractUri(name), contractAddress);
    }

    /**
     * @param uri The contract's local location
     * @param contractAddress The contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public void storeContractAddress(String uri, String contractAddress) throws IOException {
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_DEPLOY, new Object[]{uri, contractAddress});
        contracts = contractRegistry.load();
        contracts.put(uri, contracts.get(uri).setContractAddress(contractAddress));
        contracts = contractRegistry.storeContractAddresses(contracts);
    }

    /**
     * @return all contracts known by this machine node
     * @throws IOException if loading of the contracts fails
     */
    Set<Map.Entry<String,Contract>> loadContracts() throws IOException {
        contracts = contractRegistry.load();
        return contracts.entrySet();
    }

    /**
     * @param uri The contract's location
     * @return the contract
     */
    Contract getContract(String uri) {
        return contracts.get(uri);
    }

    /**
     * @param uri The contract's location
     * @return the contract
     * @throws IOException if the contract cannot be accessed
     */
    Contract validateContract(String uri) throws IOException {
        Contract contract = getContract(uri);
        if (contract.getContractAddress() == null) {
            throw new IllegalStateException("Contract address is null; deploy the contract first before using!");
        } else if (!contract.isValid()) {
            //TODO :: Replace with eth_getCode when available
            Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(contract.getContractAddress())).build();
            Types.ResponseEthGetBalance response = web3Service.ethGetBalance(request);
            return response.getBalance().equals(HexValue.toByteString(0)) ? contract.setValid(true) : contract;
        } else {
            return contract;
        }
    }

    /**
     * @param name The contract's event name
     * @return the corresponding log observable
     */
    public Observable<Types.FilterLogType> getLogObservable(N name) throws IOException {
        String uri = getContractUri(name);
        Contract contract = validateContract(uri);
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
            Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contract.getContractAddress())).build()
        ).build();
        Observable<Types.FilterLogType> ethLogObservable = web3Service.ethLogObservable(requestEthNewFilter);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_OBSERVABLE_LOG, new Object[]{uri, contract.getContractAddress()});
        return ethLogObservable;
    }

    /**
     * @param contract The contract
     * @param data The request data
     * @return the response's return value
     */
    String call(Contract contract, String data) throws IOException {
        Types.RequestEthCall request = Types.RequestEthCall.newBuilder().setTx(
            Types.TxType.newBuilder().setTo(HexValue.toByteString(contract.getContractAddress())).setData(HexValue.toByteString(data)).build()
        ).build();
        Types.ResponseEthCall response = web3Service.ethCall(request);
        return HexValue.toString(response.getReturn());
    }

    /**
     * @param contract The contract
     * @param data The request data
     */
    void sendTransaction(Contract contract, String data) throws IOException {
        String contractAddress = contract.getContractAddress();
        // eth_sendTransaction
        Types.TxType.Builder txBuilder = Types.TxType.newBuilder();
        if (contractAddress != null) {
            txBuilder.setTo(HexValue.toByteString(contractAddress));
        }
        txBuilder.setData(HexValue.toByteString(data)).setGas(HexValue.toByteString(contract.getGasLimit())).setGasPrice(HexValue.toByteString(contract.getGasPrice()));
        Types.RequestEthSendTransaction requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(txBuilder.build()).build();
        Types.ResponseEthSendTransaction responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction);
        // eth_getTransactionReceipt
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build();
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt);
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().getContractAddress());
        if (contract.getContractAddress() == null && contractAddress != null) {
            contract.setContractAddress(contractAddress);
        } else if (!Objects.equals(contract.getContractAddress(), contractAddress)) {
            throw new IllegalStateException("Returned contract address is different from known contract address!");
        }
    }
}