/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;

import java.util.concurrent.TimeUnit;

/**
 * gRPC base service.
 */
abstract class ServiceBase<S extends AbstractStub<S>, B extends AbstractStub<B>> implements Service<S,B> {
    final ManagedChannel channel;

    private ServiceBase(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
    }

    ServiceBase(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    @Override public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Override public boolean isShutdown() {
        return channel.isShutdown() || channel.isTerminated();
    }
}