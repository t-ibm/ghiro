/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.grpc;

import com.softwareag.tom.protocol.api.Burrow;

/**
 * Burrow base services over gRPC implementation.
 */
public class BurrowBaseService<T extends Service> implements Burrow {

    final T gRpcService;

    BurrowBaseService(final T gRpcService) {
        this.gRpcService = gRpcService;
    }

    public static <T extends Service> BurrowBaseService<T> build(final T supplier) {
        return new BurrowBaseService<>(supplier);
    }

    public T get() {
        return gRpcService;
    }

    @Override public void shutdown() throws InterruptedException {
        gRpcService.shutdown();
    }
}