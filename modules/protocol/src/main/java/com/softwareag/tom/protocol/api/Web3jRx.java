/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.api;

import com.softwareag.tom.protocol.abi.Types;
import rx.Observable;

/**
 * The Observables JSON-RPC client event API.
 */
public interface Web3jRx {
    /**
     * Create an observable to filter for specific log events.
     *
     * @param ethFilter The filter criteria
     * @return an observable that emits all Log events matching the filter
     */
    Observable<Types.FilterLogType> ethLogObservable(Types.RequestEthNewFilter ethFilter);
}