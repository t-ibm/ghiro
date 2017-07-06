/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.api.Web3;
import com.softwareag.tom.protocol.jsonrpc.Service;

/**
 * Web3 service API.
 */
public interface Web3Service extends Web3 {
    static Web3Service build(Service jsonRpcService) {
        return new Web3ServiceJsonRpc(jsonRpcService);
    }
}