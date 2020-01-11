/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2020 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.protocol.Web3Service;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.util.HexValue;
import com.wm.app.b2b.server.dispatcher.Dispatcher;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceSupplierWeb3 extends ServiceSupplierWeb3Base<NSName> {

    ServiceSupplierWeb3(UtilBase<NSName> util) {
        super(util);
    }

    ServiceSupplierWeb3(UtilBase<NSName> util, Web3Service web3Service) {
        super(util, web3Service);
    }

    @Override public Message<Types.FilterLogType> decodeLogEvent(Contract contract, NSName name, Types.FilterLogType logEvent) {
        IData pipeline = IDataFactory.create();
        IData envelope = IDataFactory.create();
        String uuid = "" + HexValue.toBigInteger(logEvent.getBlockNumber());
        IDataUtil.put(envelope.getCursor(),"uuid", uuid);
        IDataUtil.put(pipeline.getCursor(), Dispatcher.ENVELOPE_KEY, envelope);
        List<String> topics = logEvent.getTopicList().stream().map(HexValue::toString).collect(Collectors.toList());
        decodeEventInput(ContractSupplier.getEvent(contract, getEventName(name)), pipeline, HexValue.toString(logEvent.getData()), topics);
        return new EventMessage<>(uuid, name, logEvent, pipeline);
    }
}