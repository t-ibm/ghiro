/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.contract.Contract;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;

import java.io.IOException;

/**
 * @param <E> The event type
 * @param <O> The observer type
 * @param <S> The subscription type
 */
public interface ServiceSupplier<E,O,S> {

    /**
     * @param contract The contract
     * @param data The request data
     */
    void sendTransaction(Contract contract, String data) throws IOException;

    /**
     * @param contract The contract
     * @param data The request data
     */
    String call(Contract contract, String data) throws IOException;

    /**
     * @param contract The contract
     * @return the contract
     * @throws IOException if the contract cannot be accessed
     */
    Contract validateContract(Contract contract) throws IOException;

    /**
     * @param contract The contract
     * @param observer The event observer
     * @return a subscription for the given observer, if any
     */
    S subscribe(Contract contract, O observer);

    /**
     * @param contract The contract
     * @param eventName The contract's event name
     * @param logEvent The received log event
     * @return the log event as a {@link Message}
     */
    Message<E> decodeLogEvent(Contract contract, String eventName, E logEvent);

    /**
     * @param contract The contract
     * @param eventName The contract's event name
     * @param logEvent The received log event
     * @return {@code true} if the received log event matches the contract's event name, {@code false} otherwise
     */
    boolean isMatchingEvent(Contract contract, String eventName, E logEvent);
}
