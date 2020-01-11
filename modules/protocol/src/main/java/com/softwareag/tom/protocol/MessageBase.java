/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2020 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides basic Google Protobuf message logging.
 */
abstract class MessageBase {
    private static final Logger logger = LoggerFactory.getLogger(MessageBase.class);

    MessageBase() {}

    /**
     * Logs the request and response messages.
     * @param request The request message
     * @param response The response message
     * @param <I> The request message type
     * @param <O> The response message type
     */
    static <I extends Message, O extends Message> void log(I request, O response) {
        log(request, response.getDescriptorForType(), response);
    }
    /**
     * Logs the request and response messages.
     * @param request The request message
     * @param responseDescriptor The response message descriptor
     * @param response The response message
     * @param <I> The request message type
     */
    static <I extends Message> void log(I request, Descriptors.Descriptor responseDescriptor, Object response) {
        logger.debug(">>> {}....{}", request.getDescriptorForType().getFullName(), request);
        logger.debug("<<< {}....{}", responseDescriptor.getFullName(), response);
    }
}