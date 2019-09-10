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
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetFilterChanges;

/**
 * {@code eth_getFilterChanges}.
 */
public class RequestEthGetFilterChanges extends Request<ParamsFilter, ResponseEthGetFilterChanges> {
    public RequestEthGetFilterChanges(Service jsonRpcService, Types.RequestEthGetFilterChanges msg) {
        this(jsonRpcService, msg.getId());
    }

    public RequestEthGetFilterChanges(Service jsonRpcService, ByteString filterId) {
        super(jsonRpcService, "burrow.eventPoll", new ParamsFilter(filterId));
    }
}