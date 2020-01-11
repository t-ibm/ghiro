/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetStorageAt;

/**
 * {@code eth_getStorageAt}.
 */
public class RequestEthGetStorageAt extends Request<ParamsAddress<String>, ResponseEthGetStorageAt> {
    public RequestEthGetStorageAt(Service jsonRpcService, Types.RequestEthGetStorageAt msg) {
        super(jsonRpcService, "eth_getStorageAt", new ParamsAddress<>(msg.getAddress(), msg.getBlock().getHeight().isEmpty() ? msg.getBlock().getState().name() : msg.getBlock().getHeight().toStringUtf8()));
    }
}