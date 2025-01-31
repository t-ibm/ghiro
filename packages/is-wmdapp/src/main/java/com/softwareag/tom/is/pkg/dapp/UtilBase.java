/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.protocol.abi.Types;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import org.hyperledger.burrow.rpc.RpcEvents;

/**
 * @param <N> The contract's unique constructor, function, or event representation.
 */
public abstract class UtilBase<N> extends ContractSupplierBase<N> {

    UtilBase(String nodeName) throws ExceptionInInitializerError {
        super(nodeName);
    }

    /**
     * @return a Web3 instance
     */
    abstract ServiceSupplierWeb3Base<N> web3();

    /**
     * @return a Burrow instance
     */
    abstract ServiceSupplierBurrowBase<N> burrow();

    public <E> boolean isMatchingEvent(N name, E event) {
        if (event instanceof  Types.FilterLogType) {
            return this.web3().isMatchingEvent(name, (Types.FilterLogType)event);
        } else if (event instanceof RpcEvents.EventsResponse) {
            return this.burrow().isMatchingEvent(name, (RpcEvents.EventsResponse) event);
        }
        return false;
    }

    @SuppressWarnings("unchecked") public <E> Message<E> decodeLogEvent(N name, E event) {
        if (event instanceof  Types.FilterLogType) {
            return (Message<E>) this.web3().decodeLogEvent(name, (Types.FilterLogType)event);
        } else if (event instanceof RpcEvents.EventsResponse) {
            return (Message<E>) this.burrow().decodeLogEvent(name, (RpcEvents.EventsResponse) event);
        }
        return null;
    }
}