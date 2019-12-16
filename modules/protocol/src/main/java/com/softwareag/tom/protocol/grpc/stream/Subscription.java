/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.grpc.stream;

import io.grpc.stub.StreamObserver;

public interface Subscription {
    /**
     * Stops the receipt of notifications on the {@link StreamObserver} that was registered when this subscription; this allows deregistering
     * a {@link StreamObserver} before it has finished receiving all events (i.e. before {@link StreamObserver#onCompleted()} is called).
     */
    void unsubscribe() throws InterruptedException;

    /**
     * @return {@code true} if this {@code Subscription} is currently unsubscribed, {@code false} otherwise
     */
    boolean isUnsubscribed();
}