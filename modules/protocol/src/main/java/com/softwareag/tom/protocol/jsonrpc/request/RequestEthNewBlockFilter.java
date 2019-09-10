/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter;

/**
 * {@code eth_newBlockFilter}.
 */
public class RequestEthNewBlockFilter extends Request<ParamsEvent, ResponseEthNewFilter> {
    public RequestEthNewBlockFilter(Service jsonRpcService) {
        super(jsonRpcService, "burrow.eventSubscribe", new ParamsEvent("NewBlock"));
    }
}