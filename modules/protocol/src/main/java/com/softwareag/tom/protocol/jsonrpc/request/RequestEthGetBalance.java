/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetBalance;

import java.util.Collections;

/**
 * {@code eth_getBalance}.
 */
public class RequestEthGetBalance extends Request<String, ResponseEthGetBalance> {
    public RequestEthGetBalance(Service jsonRpcService, Types.RequestEthGetBalance msg) {
        super(jsonRpcService, "burrow.getAccount", Collections.singletonMap("address", validate(msg.getAddress())));
    }

    private static String validate(ByteString immutableByteArray) {
        if (immutableByteArray == null) {
            logger.warn("Address cannot be null.");
        } else if (immutableByteArray.size() != 20 * 2) {
            logger.warn("Address size is {0} bytes while it should be 20.", immutableByteArray.size()/2);
        } else if (!immutableByteArray.isValidUtf8()) {
            logger.warn("Address is not a valid UTF-8 encoded string.");
        } else {
            return immutableByteArray.toStringUtf8();
        }
        return null;
    }
}