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
import java.util.Map;

public abstract class ContractSupplierBase<N> implements ContractSupplier<N> {

    private UtilBase<N> util;
    ContractSupplierBase(UtilBase<N> util) {
        this.util = util;
    }

    @Override public String getContractUri(N name) { return util.getContractUri(name); }
    @Override public String getFunctionUri(N name) { return util.getFunctionUri(name); }
    @Override public String getEventUri(N name) { return util.getEventUri(name); }

    @Override public Contract getContract(String uri) { return util.getContract(uri); }

    @Override public Map<String,Contract> loadContracts() throws IOException { return util.loadContracts(); }
}