/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.grpc;

import io.grpc.stub.AbstractStub;

/**
 * gRPC service API.
 */
public interface Service<S extends AbstractStub<S>, B extends AbstractStub<B>> {
    S newStub();
    B newBlockingStub();
    void shutdown() throws InterruptedException;
    boolean isShutdown();
}