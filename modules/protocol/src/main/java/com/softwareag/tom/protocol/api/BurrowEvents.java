/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.api;

import com.softwareag.tom.protocol.BurrowService;
import com.softwareag.tom.protocol.grpc.ServiceEvents;
import io.grpc.stub.StreamObserver;
import org.hyperledger.burrow.rpc.RpcEvents;

/**
 * The query portion of the Burrow gRPC API. See the protocol buffers IDL file <a href="https://github.com/hyperledger/burrow/blob/master/protobuf/rpcevents.proto">rpcquery.proto</a> for more info.
 */
public interface BurrowEvents extends BurrowService<ServiceEvents> {

    /**
     * Request events via a non-blocking client-server gRPC.
     * @param request The request object
     * @param observer The observer to registered to listen for responses
     */
    void getEvents(RpcEvents.BlocksRequest request, StreamObserver<RpcEvents.EventsResponse> observer);
}