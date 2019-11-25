/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.grpc;

import org.hyperledger.burrow.rpc.ExecutionEventsGrpc;

/**
 * gRPC query service.
 */
public class ServiceEvents extends ServiceBase<ExecutionEventsGrpc.ExecutionEventsStub, ExecutionEventsGrpc.ExecutionEventsBlockingStub> implements Service<ExecutionEventsGrpc.ExecutionEventsStub, ExecutionEventsGrpc.ExecutionEventsBlockingStub> {

    public ServiceEvents(String host, int port) {
        super(host, port);
    }

    @Override public ExecutionEventsGrpc.ExecutionEventsStub newStub() {
        return ExecutionEventsGrpc.newStub(channel);
    }

    @Override public ExecutionEventsGrpc.ExecutionEventsBlockingStub newBlockingStub() {
        return ExecutionEventsGrpc.newBlockingStub(channel);
    }
}