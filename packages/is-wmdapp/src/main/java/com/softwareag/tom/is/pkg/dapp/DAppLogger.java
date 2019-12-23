/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.is.log.Message;
import com.wm.app.b2b.server.Server;

public final class DAppLogger {

    private DAppLogger() {}

    // Debug
    public static void logDebug(Message message, Object... params) {
        message.logAsDebug(params);
    }

    // Info
    public static void logInfo(Message message, Object... params) {
        message.logtAsInfo(params);
    }

    // Warning
    public static void logWarning(Message message, Object... params) {
        message.logAsWarn(params);
    }

    // Error
    public static void logError(Message message, Throwable e) {
        message.logAsError(e);
        Server.logError(e);
    }
    public static void logError(Message message, Object param, Throwable e) {
        message.logAsError(param, e);
        Server.logError(e);
    }
    public static void logError(Message message, Object[] params, Throwable e) {
        message.logAsError(params, e);
        Server.logError(e);
    }
}