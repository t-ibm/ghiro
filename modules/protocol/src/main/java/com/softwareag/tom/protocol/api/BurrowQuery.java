/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.api;

import com.softwareag.tom.protocol.BurrowService;
import com.softwareag.tom.protocol.grpc.ServiceQuery;
import io.grpc.stub.StreamObserver;
import org.hyperledger.burrow.Acm;
import org.hyperledger.burrow.rpc.RpcQuery;

import java.util.Iterator;

/**
 * The query portion of the Burrow gRPC API. See the protocol buffers IDL file <a href="https://github.com/hyperledger/burrow/blob/master/protobuf/rpcquery.proto">rpcquery.proto</a> for more info.
 */
public interface BurrowQuery extends BurrowService<ServiceQuery> {

    /**
     * Request account parameters via a blocking-blocking client-server gRPC.
     * @param request The request object
     * @return the response
     */
    Acm.Account getAccount(RpcQuery.GetAccountParam request);

    /**
     * Request account parameters via a blocking-streaming client-server gRPC.
     * @param request The request object
     * @return the response
     */
    Iterator<Acm.Account> listAccounts(RpcQuery.ListAccountsParam request);

    /**
     * Request account parameters via a non-blocking client-server gRPC.
     * @param request The request object
     * @param observer The observer to registered to listen for responses
     */
    void listAccounts(RpcQuery.ListAccountsParam request, StreamObserver<Acm.Account> observer);

    /**
     * Request storage value via a blocking-blocking client-server gRPC.
     * @param request The request object
     * @return the response
     */
    RpcQuery.StorageValue getStorage(RpcQuery.GetStorageParam request);
}