/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetBalance;

/**
 * {@code eth_getBalance}.
 */
public class RequestEthGetBalance extends Request<ParamsAddress, ResponseEthGetBalance> {
    public RequestEthGetBalance(Service jsonRpcService, Types.RequestEthGetBalance msg) {
        super(jsonRpcService, "eth_getBalance", new ParamsAddress(msg.getAddress(), msg.getBlock().getHeight() != ByteString.EMPTY ? msg.getBlock().getHeight().toStringUtf8() : msg.getBlock().getState().name()));
    }
}