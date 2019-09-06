/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract;

import java.io.IOException;
import java.util.Map;

/**
 * Contract registry location implementation.
 */
class ContractRegistryLocation implements ContractRegistry {

    private final ContractLocation contractLocation;
    private final ConfigLocation configLocation;

    ContractRegistryLocation(ContractLocation contractLocation, ConfigLocation configLocation) {
        this.contractLocation = contractLocation;
        this.configLocation = configLocation;
    }

    @Override public Map<String, Contract> load() throws IOException {
        Map<String, Contract> contracts = contractLocation.load();
        Map<String, String> addresses = loadContractAddresses();
        for (Map.Entry<String, String> entry : addresses.entrySet()) {
            if (contracts.containsKey(entry.getKey())) {
                contracts.get(entry.getKey()).setContractAddress(entry.getValue());
            }
        }
        return contracts;
    }

    @Override public Map<String, String> loadContractAddresses() throws IOException {
        return configLocation.loadContractAddresses();
    }

    @Override public Map<String, Contract> storeContractAddresses(Map<String, Contract> contracts) throws IOException {
        return configLocation.storeContractAddresses(contracts);
    }
}