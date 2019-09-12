/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.is.log.Message;
import com.softwareag.is.log.MessageList;
import com.softwareag.is.resources.MessageBundle;

public class DAppMsgBundle extends MessageBundle {
    private static MessageList messages = new MessageList();
    // Log messages
    public static final Message DAPP_METHOD_START = addMessage("ISP.0060.0001", "Entering method: {0}");
    public static final Message DAPP_METHOD_END = addMessage("ISP.0060.0002", "Completing method: {0}");
    static final Message DAPP_SERVICES_MKDIRS = addMessage("ISP.0060.0003", "Folder for the ns node {0} already exists.");
    static final Message DAPP_CONTRACT_DEPLOY = addMessage("ISP.0060.0004", "Deployed contract {0} to contract address {1}.");
    static final Message DAPP_CONTRACT_CALL = addMessage("ISP.0060.0005", "Called function {0}#{1} at contract address {2}.");
    static final Message DAPP_OBSERVABLE_LOG = addMessage("ISP.0060.0006", "Registered log observable for contract {0} at contract address {1}.");
    static final Message DAPP_EVENT_LOG = addMessage("ISP.0060.0007", "Received event {0}#{1} from contract address {2}.");
    // Error messages
    public static final Message DAPP_ERROR_SERVICE_ADMIN = addMessage("ISP.0060.9001", "Admin service error: {0}: {1}");
    public static final Message DAPP_ERROR_SERVICE_CONTRACT = addMessage("ISP.0060.9002", "Contract service error: {0}: {1}");
    public static final Message DAPP_ERROR_INIT = addMessage("ISP.0060.9003", "Error during initialization: {0}");
    public static final Message DAPP_ERROR_NOTIFICATION = addMessage("ISP.0060.9004", "Received error notification from observable: {0}");
    public static final Message DAPP_ERROR_PUT = addMessage("ISP.0060.9005", "Unable to add received result to message queue: {0}");
    public static final Message DAPP_ERROR_TRIGGEREXECUTION = addMessage("ISP.0060.9006", "Trigger {0} was unable to process message: Unable to start execution task: {1}");

    private static Message addMessage(String id, String text) {
        return messages.addMessage(id, text);
    }

    @Override public MessageList getMessages() {
        return messages;
    }
}