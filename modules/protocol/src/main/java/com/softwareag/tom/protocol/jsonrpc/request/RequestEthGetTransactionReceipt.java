/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetBalance;

/**
 * {@code eth_getTransactionReceipt}.
 */
public class RequestEthGetTransactionReceipt extends Request<ParamsAddress<String>, ResponseEthGetBalance> {
    public RequestEthGetTransactionReceipt(Service jsonRpcService, Types.RequestEthGetTransactionReceipt msg) {
        super(jsonRpcService, "eth_getTransactionReceipt", new ParamsAddress<>(msg.getHash()));
    }
}