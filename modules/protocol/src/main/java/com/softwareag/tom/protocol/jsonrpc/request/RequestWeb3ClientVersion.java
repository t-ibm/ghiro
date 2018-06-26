/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseWeb3ClientVersion;

import java.util.Collections;
import java.util.Map;

/**
 * {@code web3_clientVersion}.
 */
public class RequestWeb3ClientVersion extends Request<Map, ResponseWeb3ClientVersion> {
    public RequestWeb3ClientVersion(Service jsonRpcService) {
        super(jsonRpcService, "burrow.getClientVersion", Collections.emptyMap());
    }
}