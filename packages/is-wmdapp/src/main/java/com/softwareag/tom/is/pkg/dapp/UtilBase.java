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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @param <N> The contract's unique constructor, function, or event representation.
 */
public abstract class UtilBase<N> {
    private ContractRegistry contractRegistry;
    private Map<String, Contract> contracts;
    public ServiceSupplier serviceSupplier;

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
            serviceSupplier = new ServiceSupplierWeb3(node);
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
            serviceSupplier.sendTransaction(contract, contract.getBinary());
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
    Map<String,Contract> loadContracts() throws IOException {
        contracts = contractRegistry.load();
        return contracts;
    }

    /**
     * @param deployedOnly If set to {@code true} returns only events from deployed contracts, otherwise returns all defined
     * @return all contracts known by this machine node
     * @throws IOException if loading of the contracts fails
     */
    Map<String,Contract> loadContracts(boolean deployedOnly) throws IOException {
        contracts = contractRegistry.load();
        return contracts.entrySet().stream().filter(o -> !deployedOnly || o.getValue().getContractAddress() != null).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
        );
    }
    /**
     * @param uri The contract's location
     * @return the contract
     */
    Contract getContract(String uri) {
        return contracts.get(uri);
    }

    /**
     * @param name The contract's event name
     * @return the corresponding log observable
     */
    public Object getLogObservable(N name, Object observer) throws IOException {
        String uri = getContractUri(name);
        Contract contract = validateContract(uri);
        Object subscription = serviceSupplier.subscribe(contract, observer); //TODO :: Generify
        DAppLogger.logInfo(DAppMsgBundle.DAPP_OBSERVABLE_LOG, new Object[]{uri, contract.getContractAddress()});
        return subscription;
    }

    /**
     * @param name The contract's function name
     * @param data The request data
     * @return the response's return value
     */
    String call(N name, String data) throws IOException {
        String uri = getContractUri(name);
        Contract contract = validateContract(uri);
        String result = serviceSupplier.call(contract, data);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, getFunctionUri(name), contract.getContractAddress()});
        return result;
    }

    /**
     * @param name The contract's function name
     * @param data The request data
     */
    void sendTransaction(N name, String data) throws IOException {
        String uri = getContractUri(name);
        Contract contract = validateContract(uri);
        serviceSupplier.sendTransaction(contract, data);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, getFunctionUri(name), contract.getContractAddress()});
    }

    /**
     * @param uri The contract's location
     * @return the contract
     * @throws IOException if the contract cannot be accessed
     */
    private Contract validateContract(String uri) throws IOException {
        return serviceSupplier.validateContract(getContract(uri));
    }
}