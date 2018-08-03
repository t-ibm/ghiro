/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger;

import com.softwareag.tom.protocol.abi.Types;
import com.wm.app.b2b.server.ProtocolInfoIf;
import com.wm.app.b2b.server.dispatcher.AbstractExecutionTask;
import com.wm.app.b2b.server.dispatcher.exceptions.CommException;
import com.wm.app.b2b.server.dispatcher.exceptions.MessagingCoderException;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.app.b2b.server.dispatcher.trigger.control.ControlledTriggerSvcThreadPool;
import com.wm.app.b2b.server.dispatcher.trigger.control.TriggerOutputControl;
import com.wm.app.b2b.server.dispatcher.um.trigger.WmMessagingProtocolInfoImpl;
import com.wm.app.b2b.server.dispatcher.wmmessaging.ConnectionAlias;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.msg.IMessage;

public class DAppExecutionTask extends AbstractExecutionTask<Types.FilterLogType> {

    /**
     * Default constructor. Note, if the trigger has only one publishable document type associated with it we will already
     * know its name, otherwise we look it up.
     */
    DAppExecutionTask(ConnectionAlias alias, Types.FilterLogType consumerEvent, String pdtName, Trigger trigger, TriggerOutputControl toc, ControlledTriggerSvcThreadPool threadPool, boolean debugTrace) throws CommException {
        super(alias, consumerEvent, pdtName, trigger, toc, threadPool, debugTrace);
    }

    @Override protected IMessage preprocess() throws MessagingCoderException {
        return null;
    }

    @Override protected ProtocolInfoIf getProtocolInfoIf(Message<Types.FilterLogType> message) {
        return new WmMessagingProtocolInfoImpl(WmMessagingProtocolInfoImpl.SubProtocol.DES, _alias.getName(), _trigger.getName(), _pdtName, message.getDeliveryCount(), null);
    }

    @Override protected void postprocess(IMessage msg, boolean processOk, boolean shouldAck) throws Exception {

    }

    @Override protected boolean isPersistent() {
        return false;
    }
}