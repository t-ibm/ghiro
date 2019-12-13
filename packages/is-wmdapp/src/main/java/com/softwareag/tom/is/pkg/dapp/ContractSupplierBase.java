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

public abstract class ContractSupplierBase<N> implements ContractSupplier<N> {

    Node node;
    private ContractRegistry contractRegistry;
    private Map<String, Contract> contracts;

    ContractSupplierBase(String nodeName) throws ExceptionInInitializerError {
        this(Node.instance(nodeName));
    }

    private ContractSupplierBase(Node node) throws ExceptionInInitializerError {
        try {
            this.node = node;
            URI contractRegistryLocation = node.getContract().getRegistry().getLocationAsUri();
            URI configLocation = node.getConfig().getLocationAsUri();
            contractRegistry = ContractRegistry.build(new SolidityLocationFileSystem(contractRegistryLocation), new ConfigLocationFileSystem(configLocation));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override public Contract getContract(String uri) {
        return contracts.get(uri);
    }

    @Override public Map<String,Contract> loadContracts() throws IOException {
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
}