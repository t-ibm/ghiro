/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.api.BurrowTransact;
import com.softwareag.tom.protocol.grpc.ServiceTransact;

/**
 * Burrow transact services over gRPC implementation.
 */
public class BurrowServiceTransact extends BurrowServiceBase<ServiceTransact> implements BurrowTransact {

    BurrowServiceTransact(ServiceTransact gRpcService) {
        super(gRpcService);
    }
}