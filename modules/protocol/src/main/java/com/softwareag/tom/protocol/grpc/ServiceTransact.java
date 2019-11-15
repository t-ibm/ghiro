/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.grpc;

import org.hyperledger.burrow.rpc.TransactGrpc;

/**
 * gRPC transact service.
 */
public class ServiceTransact extends ServiceBase<TransactGrpc.TransactStub, TransactGrpc.TransactBlockingStub> implements Service<TransactGrpc.TransactStub, TransactGrpc.TransactBlockingStub> {

    public ServiceTransact(String host, int port) {
        super(host, port);
    }

    @Override public TransactGrpc.TransactStub newStub() {
        return TransactGrpc.newStub(channel);
    }

    @Override public TransactGrpc.TransactBlockingStub newBlockingStub() {
        return TransactGrpc.newBlockingStub(channel);
    }
}