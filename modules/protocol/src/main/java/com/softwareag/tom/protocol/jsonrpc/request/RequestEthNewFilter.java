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
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter;

/**
 * {@code eth_newFilter}.
 */
public class RequestEthNewFilter extends Request<ParamsEvent, ResponseEthNewFilter> {
    public RequestEthNewFilter(Service jsonRpcService, Types.RequestEthNewFilter msg) {
        this(jsonRpcService, msg.getOptions().getAddress());
    }

    public RequestEthNewFilter(Service jsonRpcService, ByteString address) {
        super(jsonRpcService, "burrow.eventSubscribe", new ParamsEvent(address));
    }
}