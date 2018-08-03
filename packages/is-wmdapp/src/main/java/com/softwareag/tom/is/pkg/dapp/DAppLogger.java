/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.wm.app.b2b.server.Server;
import com.wm.util.JournalLogger;

public final class DAppLogger {

    private DAppLogger() {}

    // Debug
    public static void logDebug(int messageId) { JournalLogger.logDebugPlus(messageId, DAppMsgBundle.FAC); }
    public static void logDebug(int messageId, Object param) { JournalLogger.logDebugPlus(messageId, DAppMsgBundle.FAC, param); }
    public static void logDebug(int messageId, Object[] params) { JournalLogger.logDebugPlus(messageId, DAppMsgBundle.FAC, params); }

    // Info
    public static void logInfo(int messageId) { JournalLogger.logInfo(messageId, DAppMsgBundle.FAC); }
    public static void logInfo(int messageId, Object param) { JournalLogger.logInfo(messageId, DAppMsgBundle.FAC, param); }
    public static void logInfo(int messageId, Object[] params) { JournalLogger.logInfo(messageId, DAppMsgBundle.FAC, params); }

    // Warning
    public static void logWarning(int messageId) { JournalLogger.logWarning(messageId, DAppMsgBundle.FAC); }
    public static void logWarning(int messageId, Object param) { JournalLogger.logWarning(messageId, DAppMsgBundle.FAC, param); }
    public static void logWarning(int messageId, Object[] params) { JournalLogger.logWarning(messageId, DAppMsgBundle.FAC, params); }

    // Error
    public static void logError(int messageId, Throwable e) {
        JournalLogger.logError(messageId, DAppMsgBundle.FAC, e);
        Server.logError(e);
    }
    public static void logError(int messageId, Object param, Throwable e) {
        JournalLogger.logError(messageId, DAppMsgBundle.FAC, param, e);
        Server.logError(e);
    }
    public static void logError(int messageId, Object[] params, Throwable e) {
        JournalLogger.logError(messageId, DAppMsgBundle.FAC, params, e);
        Server.logError(e);
    }
}