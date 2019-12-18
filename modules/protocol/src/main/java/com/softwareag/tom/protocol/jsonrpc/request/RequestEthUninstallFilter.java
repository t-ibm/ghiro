/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthUninstallFilter;

/**
 * {@code eth_uninstallFilter}.
 */
public class RequestEthUninstallFilter extends Request<ParamsFilter, ResponseEthUninstallFilter> {
    public RequestEthUninstallFilter(Service jsonRpcService, Types.RequestEthUninstallFilter msg) {
        this(jsonRpcService, msg.getId());
    }

    public RequestEthUninstallFilter(Service jsonRpcService, ByteString filterId) {
        super(jsonRpcService, "eth_uninstallFilter", new ParamsFilter(filterId));
    }
}