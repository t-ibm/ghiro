/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2020 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.data.IData;
import com.wm.lang.ns.NSName;
import com.wm.msg.Header;
import com.wm.util.Values;

public class EventMessage<E> extends Message<E> {

    EventMessage(String uuid, NSName name, E logEvent, IData pipeline) {
        _msgID = uuid;
        _type = name.getFullName();
        _event = logEvent;
        _data = pipeline;
    }

    @Override public Header getHeader(String name) { return null; }
    @Override public Header[] getHeaders() { return new Header[0]; }
    @Override public void setData(Object o) { _data = (IData)o; }
    @Override public Values getValues() { return Values.use(_data); }
}