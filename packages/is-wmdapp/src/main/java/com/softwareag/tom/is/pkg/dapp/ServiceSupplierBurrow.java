/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2020 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.protocol.api.BurrowEvents;
import com.softwareag.tom.protocol.api.BurrowQuery;
import com.softwareag.tom.protocol.api.BurrowTransact;
import com.softwareag.tom.protocol.util.HexValue;
import com.wm.app.b2b.server.dispatcher.Dispatcher;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import org.hyperledger.burrow.rpc.RpcEvents;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceSupplierBurrow extends ServiceSupplierBurrowBase<NSName> {

    ServiceSupplierBurrow(UtilBase<NSName> util) {
        super(util);
    }

    ServiceSupplierBurrow(UtilBase<NSName> util, BurrowQuery burrowQuery, BurrowTransact burrowTransact, BurrowEvents burrowEvents) {
        super(util, burrowQuery, burrowTransact, burrowEvents);
    }

    @Override public Message<RpcEvents.EventsResponse> decodeLogEvent(Contract contract, NSName name, RpcEvents.EventsResponse logEvent) {
        IData pipeline = IDataFactory.create();
        IData envelope = IDataFactory.create();
        String uuid = "" + logEvent.getEvents(0).getHeader().getHeight();
        IDataUtil.put(envelope.getCursor(),"uuid", uuid);
        IDataUtil.put(pipeline.getCursor(), Dispatcher.ENVELOPE_KEY, envelope);
        List<String> topics = logEvent.getEvents(0).getLog().getTopicsList().stream().map(t -> HexValue.toString(t.toByteArray())).collect(Collectors.toList());
        decodeEventInput(ContractSupplier.getEvent(contract, getEventName(name)), pipeline, HexValue.toString(logEvent.getEvents(0).getLog().getData().toByteArray()), topics);
        return new EventMessage<>(uuid, name, logEvent, pipeline);
    }
}