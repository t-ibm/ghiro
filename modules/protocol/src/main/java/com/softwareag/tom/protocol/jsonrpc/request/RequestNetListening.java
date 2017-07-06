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
import com.softwareag.tom.protocol.jsonrpc.response.ResponseNetListening;

import java.util.Collections;

/**
 * {@code net_listening}.
 */
public class RequestNetListening extends Request<String, ResponseNetListening> {
    public RequestNetListening(Service jsonRpcService, Types.RequestNetListening msg) {
        super(jsonRpcService, "burrow.isListening", Collections.emptyList(), DEFAULT_CORRELATION_ID);
    }
}