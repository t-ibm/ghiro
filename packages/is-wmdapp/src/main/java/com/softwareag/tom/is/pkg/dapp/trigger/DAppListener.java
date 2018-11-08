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
import com.softwareag.tom.is.pkg.dapp.Util;
import com.softwareag.tom.protocol.abi.Types;
import com.wm.app.b2b.server.dispatcher.AbstractListener;
import com.wm.app.b2b.server.dispatcher.MessageListenerQueue;
import com.wm.app.b2b.server.dispatcher.exceptions.CommException;
import com.wm.app.b2b.server.dispatcher.exceptions.MessagingSubsystemException;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.app.b2b.server.dispatcher.trigger.control.ControlledTriggerSvcThreadPool;
import com.wm.app.b2b.server.dispatcher.wmmessaging.ConnectionAlias;
import com.wm.app.b2b.server.resources.MessagingBundle;
import com.wm.lang.ns.NSName;
import rx.Observable;
import rx.Subscription;

import java.io.IOException;

public class DAppListener extends AbstractListener<Types.FilterLogType> {

    public static final String IS_DAPP_CONNECTION = "IS_DAPP_CONNECTION";

    private Observable<Types.FilterLogType> logObservable;
    private Subscription subscription;

    /**
     * @param trigger The trigger
     * @param threadPool The trigger thread pool partition from the global IS Service thread pool
     */
    public DAppListener(Trigger trigger, ControlledTriggerSvcThreadPool threadPool) throws CommException {
        super(trigger, threadPool);
    }

    @Override protected MessageListenerQueue<Types.FilterLogType> getQueue() { return super.getQueue(); }

    @Override protected ConnectionAlias getConnectionAlias() {
        return null;
    }

    @Override protected void createListener() throws CommException {
        DAppLogger.logDebug(DAppMsgBundle.DAPP_METHOD_START, "Listener#createListener");

        if (_channelFilterPairs == null || _channelFilterPairs.isEmpty() || _channelFilterPairs.size() != 1) {
            throw new MessagingSubsystemException(MessagingBundle.MISSING_REQUIRED_PARAMETER, "Listener#createListener channelFilterPairs");
        }

        NSName pdt = NSName.create(_channelFilterPairs.get(0).getPdtName());
        try {
            logObservable = Util.instance.getLogObservable(pdt);
        } catch (IOException e) {
            DAppLogger.logError(DAppMsgBundle.DAPP_ERROR_INIT, e);
            _messageListenerRunning = false;
            throw new MessagingSubsystemException(e);
        }

        subscription = logObservable.subscribe(
            // On Next
            result -> {
                try {
                    if (Util.instance.isMatchingEvent(pdt, result)) {
                        _messageQueue.put(result);
                    }
                } catch (InterruptedException e) {
                    //  The put operation failed. This should not happen, but if the trigger is still running, then we will try again.
                    if (isRunning()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {}

                        try {
                            _messageQueue.put(result);
                        } catch (InterruptedException e2) {
                            // If we fail again, then we have a bigger problem, so log the error and roll back the message.
                            DAppLogger.logError(DAppMsgBundle.DAPP_ERROR_PUT, e2);
                        }
                    }
                }
                // If we are stopping the Listener, then we do not want to receive any new messages.
                // Note that the the latch should always get posted on shutdown, but we add the timeout just in case there is some odd exception.
                pauseProcessing();
            },
            // On Error
            throwable -> DAppLogger.logError(DAppMsgBundle.DAPP_ERROR_NOTIFICATION, throwable),
            // On Completed
            this::stopProcessing
        );

        for (int i = 0; i < _channelFilterPairs.size(); i++) {
            //TODO
        }
        _messageListenerRunning = true;
        DAppLogger.logDebug(DAppMsgBundle.DAPP_METHOD_END, "Listener#createListener");
    }

    /**
     * Consume the messages from the trigger queue and process the messages (repeat until the Listener is stopped). If the
     * trigger's retrieval is suspended then start the TriggerQueueConsumer in suspended mode.
     */
    @Override protected void initMessageDispatcher() {
        String pdtName = _channelFilterPairs.get(0).getPdtName();

        DAppMessageDispatcher messageDispatcher = new DAppMessageDispatcher("0", pdtName, this);

        if (_trigger.isProcessingSuspended()) {
            suspendProcessing();
        }
        messageDispatcher.consumeAndProcessMessages();
    }

    @Override protected void stopListener(boolean deleteSubscription) throws Exception {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        logObservable = null;
        super.stopListener();
    }

    @Override public void unsubscribe() {
        //TODO
    }
}