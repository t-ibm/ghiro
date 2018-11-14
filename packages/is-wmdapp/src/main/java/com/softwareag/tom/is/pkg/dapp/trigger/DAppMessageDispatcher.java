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
import com.wm.app.b2b.server.dispatcher.AbstractMessageDispatcher;
import com.wm.app.b2b.server.dispatcher.um.trigger.UMChannelFilterPair;
import com.wm.lang.ns.NSName;

import java.util.List;

public class DAppMessageDispatcher extends AbstractMessageDispatcher<Types.FilterLogType> {

    private List<UMChannelFilterPair> channelFilterPairs;
    /**
     * @param id The message dispatcher id
     * @param channelFilterPairs The publishable document type names
     * @param listener The message listener reference
     */
    DAppMessageDispatcher(String id, List<UMChannelFilterPair> channelFilterPairs, AbstractListener<Types.FilterLogType> listener) {
        super(id, channelFilterPairs.get(0).getPdtName(), listener);
        this.channelFilterPairs = channelFilterPairs;
    }

    @Override protected boolean processNextMessage() {
        Types.FilterLogType consumerEvent = _queue.poll();
        if (consumerEvent != null) {

            _listener.checkForThrottling();

            String pdtName = null;
            for (UMChannelFilterPair channelFilterPair : channelFilterPairs) {
                pdtName = channelFilterPair.getPdtName();
                if (Util.instance.isMatchingEvent(NSName.create(pdtName), consumerEvent)) {
                    break;
                }
            }

            DAppExecutionTask task;
            try {
                task = new DAppExecutionTask(consumerEvent, pdtName, _listener);
                _toc.prepareToExecuteUMTrigger(); // This will block until a trigger thread is available

                if (_isSerial && _listener.isRetrievalSuspended()) {
                    //roll back will happen automatically when subscription is stopped
                    _toc.onComplete(false);
                }else {
                    _threadPool.runTarget(task);
                }

            } catch (Throwable t) {
                DAppLogger.logError(DAppMsgBundle.DAPP_ERROR_TRIGGEREXECUTION, _trigger.getName(), t);
            }
            return true;
        }else {
            return false;
        }
    }
}