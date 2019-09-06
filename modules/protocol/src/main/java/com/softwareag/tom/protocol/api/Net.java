/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.api;

import com.softwareag.tom.protocol.abi.Types;

import java.io.IOException;

/**
 * The net portion of the Ethereum JSON-RPC API. See the <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC">JSON-RPC Wiki</a> for more info.
 */
interface Net {
    /**
     * Method {@code net_listening}.
     * @return {@code true} when listening, otherwise {@code false}
     * @throws IOException in case of an error
     */
    Types.ResponseNetListening netListening() throws IOException;
}