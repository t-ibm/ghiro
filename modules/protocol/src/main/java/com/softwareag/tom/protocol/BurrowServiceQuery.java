/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.api.BurrowQuery;
import com.softwareag.tom.protocol.grpc.ServiceQuery;
import io.grpc.stub.StreamObserver;
import org.hyperledger.burrow.Acm;
import org.hyperledger.burrow.rpc.RpcQuery;

import java.util.Iterator;

/**
 * Burrow query services over gRPC implementation.
 */
public class BurrowServiceQuery extends BurrowServiceBase<ServiceQuery> implements BurrowQuery {

    BurrowServiceQuery(ServiceQuery gRpcService) {
        super(gRpcService);
    }

    @Override public Acm.Account getAccount(RpcQuery.GetAccountParam request) {
        Acm.Account response = gRpcService.newBlockingStub().getAccount(request);
        log(request, response);
        return response;
    }

    @Override public Iterator<Acm.Account> listAccounts(RpcQuery.ListAccountsParam request) {
        Iterator<Acm.Account> response = gRpcService.newBlockingStub().listAccounts(request);
        log(request, Acm.Account.getDescriptor(), response);
        return response;
    }

    @Override public void listAccounts(RpcQuery.ListAccountsParam request, StreamObserver<Acm.Account> observer) {
        log(request, Acm.Account.getDescriptor(), observer);
        gRpcService.newStub().listAccounts(request, observer);
    }

    @Override public RpcQuery.StorageValue getStorage(RpcQuery.GetStorageParam request) {
        RpcQuery.StorageValue response = gRpcService.newBlockingStub().getStorage(request);
        log(request, response);
        return response;
    }
}