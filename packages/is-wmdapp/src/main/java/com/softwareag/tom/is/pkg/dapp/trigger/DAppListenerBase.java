/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger;

import com.softwareag.tom.is.pkg.dapp.DAppLogger;
import com.softwareag.tom.is.pkg.dapp.DAppMsgBundle;
import com.wm.app.b2b.server.dispatcher.AbstractListener;
import com.wm.app.b2b.server.dispatcher.exceptions.CommException;
import com.wm.app.b2b.server.dispatcher.exceptions.MessagingSubsystemException;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.app.b2b.server.dispatcher.trigger.control.ControlledTriggerSvcThreadPool;
import com.wm.app.b2b.server.dispatcher.wmmessaging.ConnectionAlias;
import com.wm.app.b2b.server.resources.MessagingBundle;

import java.io.IOException;

public abstract class DAppListenerBase<E> extends AbstractListener<E> {

    public static final String IS_DAPP_CONNECTION = "IS_DAPP_CONNECTION";

    /**
     * @param trigger The trigger
     * @param threadPool The trigger thread pool partition from the global IS Service thread pool
     */
    DAppListenerBase(Trigger trigger, ControlledTriggerSvcThreadPool threadPool) throws CommException {
        super(trigger, threadPool);
    }

    /**
     * Registers an observer with this subscription.
     * @throws IOException in case the stream from the provider got interrupted
     */
    abstract void subscribe() throws IOException;

    @Override protected ConnectionAlias getConnectionAlias() {
        return null;
    }

    @Override protected void createListener() throws CommException {
        DAppLogger.logDebug(DAppMsgBundle.DAPP_METHOD_START, "Listener#createListener");

        if (_channelFilterPairs == null || _channelFilterPairs.isEmpty()) {
            throw new MessagingSubsystemException(MessagingBundle.MISSING_REQUIRED_PARAMETER, "Listener#createListener channelFilterPairs");
        }

        try {
            subscribe();
        } catch (IOException e) {
            DAppLogger.logError(DAppMsgBundle.DAPP_ERROR_INIT, e);
            _messageListenerRunning = false;
            throw new MessagingSubsystemException(e);
        }

        _messageListenerRunning = true;
        DAppLogger.logDebug(DAppMsgBundle.DAPP_METHOD_END, "Listener#createListener");
    }

    /**
     * Consume the messages from the trigger queue and process the messages (repeat until the Listener is stopped). If the
     * trigger's retrieval is suspended then start the TriggerQueueConsumer in suspended mode.
     */
    @Override protected void initMessageDispatcher() {
        DAppMessageDispatcher<E> messageDispatcher = new DAppMessageDispatcher<>("0", this);

        if (_trigger.isProcessingSuspended()) {
            suspendProcessing();
        }
        messageDispatcher.consumeAndProcessMessages();
    }

    @Override protected void stopListener(boolean deleteSubscription) {
        unsubscribe();
        super.stopListener();
    }
}