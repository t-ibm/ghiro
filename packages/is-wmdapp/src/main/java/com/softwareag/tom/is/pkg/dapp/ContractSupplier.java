/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.contract.abi.ContractInterface;

import java.io.IOException;
import java.util.Map;

public interface ContractSupplier<N> {

    /**
     * @param name The fully-qualified contract's constructor, function, or event name
     * @return the contract's URI
     */
    String getContractUri(N name);

    /**
     * @param name The fully-qualified contract's function name
     * @return the contract's function name
     */
    String getFunctionName(N name);

    /**
     * @param name The fully-qualified contract's event name
     * @return the contract's event name
     */
    String getEventName(N name);

    /**
     * @param uri The contract's location
     * @return the contract
     */
    Contract getContract(String uri);

    /**
     * @return all contracts known by this machine node
     * @throws IOException if loading of the contracts fails
     */
    Map<String,Contract> loadContracts() throws IOException;

    /**
     * @param contract The contract
     * @param functionName The contract's function name
     * @return the contract's function specification
     */
    static ContractInterface.Specification<?> getFunction(Contract contract, String functionName) {
        ContractInterface.Specification<?> function = contract.getAbi().getFunctions().stream().filter(o -> o.getName().equals(functionName)).findFirst().orElse(null);
        assert function != null;
        return function;
    }

    /**
     * @param contract The contract
     * @param eventName The contract's event name
     * @return the contract's event specification
     */
    static ContractInterface.Specification<?> getEvent(Contract contract, String eventName) {
        ContractInterface.Specification<?> event = contract.getAbi().getEvents().stream().filter(o -> o.getName().equals(eventName)).findFirst().orElse(null);
        assert event != null;
        return event;
    }
}