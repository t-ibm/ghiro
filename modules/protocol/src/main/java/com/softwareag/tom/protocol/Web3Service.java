/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.api.Web3;
import com.softwareag.tom.protocol.api.Web3jRx;
import com.softwareag.tom.protocol.jsonrpc.Service;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Web3 service API.
 */
public interface Web3Service extends Web3, Web3jRx {
    static Web3Service build(Service jsonRpcService) {
        return new Web3ServiceJsonRpc(jsonRpcService);
    }
    static Web3Service build(Service jsonRpcService, long pollingInterval, ScheduledExecutorService scheduledExecutorService) {
        return new Web3ServiceJsonRpc(jsonRpcService, pollingInterval, scheduledExecutorService);
    }
}