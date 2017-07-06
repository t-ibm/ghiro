/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON-RPC base service implementation.
 */
abstract class ServiceBase implements Service {

    final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
}