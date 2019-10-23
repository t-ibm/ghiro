/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.api.BurrowQuery;
import com.softwareag.tom.protocol.api.BurrowTransact;
import com.softwareag.tom.protocol.grpc.BurrowQueryService;
import com.softwareag.tom.protocol.grpc.BurrowTransactService;
import com.softwareag.tom.protocol.grpc.ServiceQuery;
import com.softwareag.tom.protocol.grpc.ServiceTransact;

/**
 * Burrow service API.
 */
public interface BurrowService {

    static BurrowQuery query(ServiceQuery gRpcService) {
        return new BurrowQueryService(gRpcService);
    }

    static BurrowTransact transact(ServiceTransact gRpcService) {
        return new BurrowTransactService(gRpcService);
    }
}