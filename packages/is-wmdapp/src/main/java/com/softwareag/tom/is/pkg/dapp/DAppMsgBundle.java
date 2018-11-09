/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.wm.util.B2BListResourceBundle;

public class DAppMsgBundle extends B2BListResourceBundle {

    static final int FAC = 60;//JournalLogger.FAC_DAPP; //TODO

    // Keys
    public static final int DAPP_METHOD_START = 1;
    public static final int DAPP_METHOD_END = 2;
    static final int DAPP_SERVICES_MKDIRS = 3;
    static final int DAPP_CONTRACT_DEPLOY = 4;
    static final int DAPP_CONTRACT_CALL = 5;
    static final int DAPP_OBSERVABLE_LOG = 6;
    static final int DAPP_EVENT_LOG = 7;
    public static final int DAPP_TRIGGER_WAITFORTHROTTLED = 8;

    public static final int DAPP_ERROR_INIT = 9001;
    public static final int DAPP_ERROR_NOTIFICATION = 9002;
    public static final int DAPP_ERROR_PUT = 9003;
    public static final int DAPP_ERROR_TRIGGEREXECUTION = 9004;

    // Values
    private static final Object[][] contents = new Object[][]{
        // Log messages
        {FAC + "." + DAPP_METHOD_START, "Entering method: {0}"},
        {FAC + "." + DAPP_METHOD_END, "Completing method: (0)"},
        {FAC + "." + DAPP_CONTRACT_DEPLOY, "Deployed contract {0} to contract address {1}."},
        {FAC + "." + DAPP_CONTRACT_CALL, "Called function {0}#{1} at contract address {2}."},
        {FAC + "." + DAPP_OBSERVABLE_LOG, "Registered log observable for contract {0} at contract address {1}."},
        {FAC + "." + DAPP_EVENT_LOG, "Received event {0}#{1} from contract address {2}."},
        {FAC + "." + DAPP_TRIGGER_WAITFORTHROTTLED, "Trigger {0} waiting for throttled queue capacity to become available."},
        // Error messages
        {FAC + "." + DAPP_ERROR_INIT, "Error during initialization: {0}"},
        {FAC + "." + DAPP_ERROR_NOTIFICATION, "Received error notification from observable: {0}"},
        {FAC + "." + DAPP_ERROR_PUT, "Unable to add received result to message queue: {0}"},
        {FAC + "." + DAPP_ERROR_TRIGGEREXECUTION, "Trigger {0} was unable to process message: Unable to start execution task: {1}" },
    };

    @Override protected Object[][] getContents() {
        return contents;
    }
}