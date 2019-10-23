/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.grpc;

import com.softwareag.tom.protocol.api.BurrowQuery;
import org.hyperledger.burrow.Acm;
import org.hyperledger.burrow.rpc.QueryGrpc;
import org.hyperledger.burrow.rpc.RpcQuery;

/**
 * Burrow query services over gRPC implementation.
 */
public class BurrowQueryService implements BurrowQuery {

    private final ServiceQuery gRpcService;

    public BurrowQueryService(ServiceQuery gRpcService) {
        this.gRpcService = gRpcService;
    }

    @Override public void shutdown() throws InterruptedException {
        gRpcService.shutdown();
    }

    @Override public Acm.Account getAccount(RpcQuery.GetAccountParam req) {
        QueryGrpc.QueryBlockingStub stub = gRpcService.newBlockingStub();
        return stub.getAccount(req);
    }
}