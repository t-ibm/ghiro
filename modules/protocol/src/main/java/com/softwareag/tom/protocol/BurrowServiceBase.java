/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.grpc.Service;

/**
 * Burrow base services over gRPC implementation.
 */
public class BurrowServiceBase<T extends Service> extends MessageBase implements BurrowService<T> {

    final T gRpcService;

    BurrowServiceBase(final T gRpcService) {
        this.gRpcService = gRpcService;
    }

    @Override public T getService() {
        return gRpcService;
    }
}