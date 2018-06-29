/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.filter;

import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetFilterChanges;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter;

import java.io.IOException;
import java.util.List;

/**
 * Log filter handler.
 */
public class LogFilter extends Filter<ResponseEthGetFilterChanges.Log> {

    private final RequestEthNewFilter ethFilter;

    public LogFilter(Service jsonRpcService, Callback<ResponseEthGetFilterChanges.Log> callback, RequestEthNewFilter ethFilter) {
        super(jsonRpcService, callback);
        this.ethFilter = ethFilter;
    }

    @Override ResponseEthNewFilter sendRequest() throws IOException {
        return ethFilter.send();
    }

    @Override void process(List<ResponseEthGetFilterChanges.Event> logResults) {
        for (ResponseEthGetFilterChanges.Event logResult : logResults) {
            if (logResult instanceof ResponseEthGetFilterChanges.Log) {
                ResponseEthGetFilterChanges.Log log = ((ResponseEthGetFilterChanges.Log) logResult).get();
                callback.onEvent(log);
            } else {
                throw new FilterException("Unexpected result type: " + logResult.get() + " required LogObject");
            }
        }
    }
}