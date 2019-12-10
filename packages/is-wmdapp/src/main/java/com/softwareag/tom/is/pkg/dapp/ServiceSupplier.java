/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.contract.Contract;

import java.io.IOException;

public interface ServiceSupplier<O,S> {

    /**
     * @param contract The contract
     * @param data The request data
     */
    void sendTransaction(Contract contract, String data) throws IOException;

    /**
     * @param contract The contract
     * @param data The request data
     */
    String call(Contract contract, String data) throws IOException;

    /**
     * @param contract The contract
     * @return the contract
     * @throws IOException if the contract cannot be accessed
     */
    Contract validateContract(Contract contract) throws IOException;

    S subscribe(Contract contract, O observer);
}
