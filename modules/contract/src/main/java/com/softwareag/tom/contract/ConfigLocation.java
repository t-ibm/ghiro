/*
 * Copyright (c) 2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract;

import java.io.IOException;
import java.util.Map;

/**
 * Config location API.
 */
public interface ConfigLocation {
    Map<String, String> loadContractAddresses() throws IOException;
    Map<String, String> storeContractAddresses(Map<String, Contract> contracts) throws IOException;
}