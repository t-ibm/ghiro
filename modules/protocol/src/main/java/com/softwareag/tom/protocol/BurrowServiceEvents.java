/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.api.BurrowEvents;
import com.softwareag.tom.protocol.grpc.ServiceEvents;
import io.grpc.stub.StreamObserver;
import org.hyperledger.burrow.rpc.RpcEvents;

/**
 * Burrow query services over gRPC implementation.
 */
public class BurrowServiceEvents extends BurrowServiceBase<ServiceEvents> implements BurrowEvents {

    BurrowServiceEvents(ServiceEvents gRpcService) {
        super(gRpcService);
    }

    @Override public void getEvents(RpcEvents.BlocksRequest request, StreamObserver<RpcEvents.EventsResponse> observer) {
        log(request, RpcEvents.EventsResponse.getDescriptor(), observer);
        gRpcService.newStub().events(request, observer);
    }
}