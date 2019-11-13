/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseNetListening;

import java.util.Collections;
import java.util.List;

/**
 * {@code net_listening}.
 */
public class RequestNetListening extends Request<List, ResponseNetListening> {
    public RequestNetListening(Service jsonRpcService) {
        super(jsonRpcService, "net_listening", Collections.emptyList());
    }
}