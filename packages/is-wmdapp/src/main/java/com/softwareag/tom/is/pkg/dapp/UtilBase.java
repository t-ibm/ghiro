/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

/**
 * @param <N> The contract's unique constructor, function, or event representation.
 */
public abstract class UtilBase<N> extends ContractSupplierBase<N> {

    public ServiceSupplierWeb3<N> web3;
    public ServiceSupplierBurrow<N> burrow;

    UtilBase(String nodeName) throws ExceptionInInitializerError {
        super(nodeName);
    }

    public ServiceSupplierWeb3<N> web3() {
        if (web3 == null) {
            web3 = new ServiceSupplierWeb3<>(this);
        }
        return web3;
    }

    public ServiceSupplierBurrow<N> burrow() {
        if (burrow == null) {
            burrow = new ServiceSupplierBurrow<>(this);
        }
        return burrow;
    }
}