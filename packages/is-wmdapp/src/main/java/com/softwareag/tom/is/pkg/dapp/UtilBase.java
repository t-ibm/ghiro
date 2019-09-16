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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public abstract class UtilBase {
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
}