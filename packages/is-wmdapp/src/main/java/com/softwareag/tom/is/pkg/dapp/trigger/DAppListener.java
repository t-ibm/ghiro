/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger;

import com.softwareag.tom.is.pkg.dapp.DAppLogger;
import com.softwareag.tom.is.pkg.dapp.DAppMsgBundle;
import com.softwareag.tom.is.pkg.dapp.Util;
import com.softwareag.tom.protocol.abi.Types;
import com.wm.app.b2b.server.dispatcher.exceptions.CommException;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.app.b2b.server.dispatcher.trigger.control.ControlledTriggerSvcThreadPool;
import rx.Observer;
import rx.Subscription;

import java.io.IOException;

public class DAppListener extends DAppListenerBase<Types.FilterLogType> {

    private Subscription subscription;

    public DAppListener(Trigger trigger, ControlledTriggerSvcThreadPool threadPool) throws CommException {
        super(trigger, threadPool);
    }

    @Override void subscribe() throws IOException {
        Observer<Types.FilterLogType> observer = new Observer<Types.FilterLogType>() {

            @Override public void onCompleted() {
                stopProcessing();
            }

            @Override public void onError(Throwable throwable) {
                DAppLogger.logError(DAppMsgBundle.DAPP_ERROR_NOTIFICATION, throwable);
            }

            @Override public void onNext(Types.FilterLogType result) {
                try {
                    _messageQueue.put(result);
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
            }
        };
        subscription = Util.instance().web3().subscribe(_trigger.getNSName(), observer);
    }

    @Override public void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}