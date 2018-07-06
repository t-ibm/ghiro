/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc;

import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.filter.Filter;
import com.softwareag.tom.protocol.jsonrpc.filter.LogFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.ScheduledExecutorService;

/**
 * JSON-RPC observables implementation.
 */
public class JsonRpcRx {

    private final Service jsonRpcService;
    private final ScheduledExecutorService scheduledExecutorService;

    public JsonRpcRx(Service jsonRpcService, ScheduledExecutorService scheduledExecutorService) {
        this.jsonRpcService = jsonRpcService;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public Observable<Types.FilterLogType> ethLogObservable(RequestEthNewFilter requestEthNewFilter, long pollingInterval) {
        return Observable.create((Subscriber<? super Types.FilterLogType> subscriber) -> {
            LogFilter logFilter = new LogFilter(jsonRpcService, subscriber::onNext, requestEthNewFilter);

            run(logFilter, subscriber, pollingInterval);
        });
    }

    private <T> void run(Filter<T> filter, Subscriber<? super T> subscriber, long pollingInterval) {
        filter.run(scheduledExecutorService, pollingInterval);
        subscriber.add(Subscriptions.create(filter::cancel));
    }
}