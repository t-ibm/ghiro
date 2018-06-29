/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.filter;

import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.jsonrpc.Response;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetFilterChanges;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthUninstallFilter;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetFilterChanges;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthUninstallFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Filter handler base.
 */
public abstract class Filter<T> {

    private final Service jsonRpcService;
    final Callback<T> callback;

    private volatile ByteString filterId;

    private ScheduledFuture<?> schedule;

    public Filter(Service jsonRpcService, Callback<T> callback) {
        this.jsonRpcService = jsonRpcService;
        this.callback = callback;
    }

    public void run(ScheduledExecutorService scheduledExecutorService, long blockTime) {
        try {
            ResponseEthNewFilter ethFilter = sendRequest();
            if (ethFilter.hasError()) {
                throwException(ethFilter.getError());
            }

            filterId = ethFilter.getFilterId();

            schedule = scheduledExecutorService.scheduleAtFixedRate(() -> {
                ResponseEthGetFilterChanges ethLog = null;
                try {
                    RequestEthGetFilterChanges jsonRpcRequest = new RequestEthGetFilterChanges(jsonRpcService, filterId) {};
                    ethLog = jsonRpcRequest.send();
                } catch (IOException e) {
                    throwException(e);
                }
                if (ethLog.hasError()) {
                    throwException(ethFilter.getError());
                }

                process(ethLog.getEvents());
            }, 0, blockTime, TimeUnit.MILLISECONDS);


        } catch (IOException e) {
            throwException(e);
        }
    }

    abstract ResponseEthNewFilter sendRequest() throws IOException;

    abstract void process(List<ResponseEthGetFilterChanges.Event> logResults);

    public void cancel() {
        schedule.cancel(false);

        ResponseEthUninstallFilter ethUninstallFilter = null;
        try {
            RequestEthUninstallFilter jsonRpcRequest = new RequestEthUninstallFilter(jsonRpcService, filterId) {};
            ethUninstallFilter = jsonRpcRequest.send();
        } catch (IOException e) {
            throwException(e);
        }

        if (ethUninstallFilter.hasError()) {
            throwException(ethUninstallFilter.getError());
        }

        if (!ethUninstallFilter.isRemoved()) {
            throwException(ethUninstallFilter.getError());
        }
    }

    private void throwException(Response.Error error) {
        throw new FilterException("Invalid request: " + error.message);
    }

    private void throwException(Throwable cause) {
        throw new FilterException("Error sending request", cause);
    }
}