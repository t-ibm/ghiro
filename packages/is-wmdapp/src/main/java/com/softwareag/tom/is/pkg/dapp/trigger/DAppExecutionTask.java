/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger;

import com.softwareag.tom.is.pkg.dapp.Util;
import com.wm.app.b2b.server.ProtocolInfoIf;
import com.wm.app.b2b.server.dispatcher.AbstractExecutionTask;
import com.wm.app.b2b.server.dispatcher.AbstractListener;
import com.wm.app.b2b.server.dispatcher.exceptions.MessagingCoderException;
import com.wm.app.b2b.server.dispatcher.um.trigger.WmMessagingProtocolInfoImpl;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.lang.ns.NSName;
import com.wm.msg.IMessage;

import static com.softwareag.tom.is.pkg.dapp.trigger.DAppListener.IS_DAPP_CONNECTION;

public class DAppExecutionTask<E> extends AbstractExecutionTask<E> {

    /**
     * Default constructor. Note, if the trigger has only one publishable document type associated with it we will already
     * know its name, otherwise we look it up.
     */
    DAppExecutionTask(E consumerEvent, String pdtName, AbstractListener<E> listener) {
        super(consumerEvent, pdtName, listener);
    }

    @Override protected IMessage preprocess() throws MessagingCoderException {
        NSName nsName = NSName.create(_pdtName);
        try {
            Util<E,?,?> util = Util.instance();
            Message<E> msg = util.decodeLogEvent(nsName, _event);
            setup(msg);
            return msg;
        } catch (Exception e) {
            throw new MessagingCoderException(e);
        }
    }

    @Override protected ProtocolInfoIf getProtocolInfoIf(Message<E> message) {
        //TODO :: Change DES to DApp
        return new WmMessagingProtocolInfoImpl(WmMessagingProtocolInfoImpl.SubProtocol.DES, IS_DAPP_CONNECTION, _trigger.getName(), _pdtName, message.getDeliveryCount(), null);
    }

    @Override protected void postprocess(IMessage msg, boolean processOk, boolean shouldAck) throws Exception {

    }

    @Override protected boolean isPersistent() {
        return false;
    }
}