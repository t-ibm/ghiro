/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.filter;

import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetFilterChanges;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter;
import com.softwareag.tom.protocol.util.HexValue;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Log filter handler.
 */
public class LogFilter extends Filter<Types.FilterLogType> {

    private final RequestEthNewFilter ethFilter;

    public LogFilter(Service jsonRpcService, Callback<Types.FilterLogType> callback, RequestEthNewFilter ethFilter) {
        super(jsonRpcService, callback);
        this.ethFilter = ethFilter;
    }

    @Override ResponseEthNewFilter sendRequest() throws IOException {
        return ethFilter.send();
    }

    @Override void process(List<ResponseEthGetFilterChanges.Event> events) {
        for (ResponseEthGetFilterChanges.Event event : events) {
            if (event instanceof ResponseEthGetFilterChanges.Log) {
                ResponseEthGetFilterChanges.Log logEvent = ((ResponseEthGetFilterChanges.Log) event).get();
                callback.onEvent(Types.FilterLogType.newBuilder()
                    .setAddress(HexValue.toByteString(logEvent.address))
                    .setData(HexValue.toByteString(logEvent.data))
                    .setBlockNumber(HexValue.toByteString(logEvent.height))
                    .addAllTopic(logEvent.topics.stream().map(HexValue::toByteString).collect(Collectors.toList()))
                    .build()
                );
            } else {
                throw new FilterException("Unexpected result type: " + event.get() + " required LogObject");
            }
        }
    }
}