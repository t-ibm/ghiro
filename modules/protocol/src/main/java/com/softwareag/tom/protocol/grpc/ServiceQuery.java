/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.grpc;

import org.hyperledger.burrow.rpc.QueryGrpc;

/**
 * gRPC query service.
 */
public class ServiceQuery extends ServiceBase<QueryGrpc.QueryStub,QueryGrpc.QueryBlockingStub> implements Service<QueryGrpc.QueryStub,QueryGrpc.QueryBlockingStub> {

    public ServiceQuery(String host, int port) {
        super(host, port);
    }

    @Override public QueryGrpc.QueryStub newStub() {
        return QueryGrpc.newStub(channel);
    }

    @Override public QueryGrpc.QueryBlockingStub newBlockingStub() {
        return QueryGrpc.newBlockingStub(channel);
    }
}