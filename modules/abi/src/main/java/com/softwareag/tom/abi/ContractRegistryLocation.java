/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.abi;

import java.io.IOException;
import java.util.Map;

/**
 * Contract registry location implementation.
 */
class ContractRegistryLocation implements ContractRegistry {

    private final ContractLocation location;

    ContractRegistryLocation(ContractLocation location) {
        this.location = location;
    }

    @Override public Map<String, Contract> load() throws IOException {
        return location.load();
    }
}