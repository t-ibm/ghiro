/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.api;

import com.google.protobuf.Message;
import com.softwareag.tom.protocol.abi.Types;

/**
 * The eth portion of the Ethereum JSON-RPC API. See the <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC">JSON-RPC Wiki</a> for more info.
 */
interface Eth {
    /**
     * Method {@code eth_getBalance}.
     * @param req A request object containing the address to check for balance as well as a parameter of type {@link Types.DefaultBlockType}
     * @return the integer of the current balance in wei
     */
    Message ethGetBalance(Types.RequestEthGetBalance req);

    /**
     * Method {@code eth_sendTransaction}.
     * @param req A request object containing the transaction object of type {@link Types.TxType}
     * @return the transaction hash, or the zero hash if the transaction is not yet available
     */
    Message ethSendTransaction(Types.RequestEthSendTransaction req);

    /**
     * Method {@code eth_call}.
     * @param req A request object containing the transaction object of type {@link Types.TxType} as well as a parameter of type {@link Types.DefaultBlockType}
     * @return the return value of executed contract
     */
    Message ethCall(Types.RequestEthCall req);

    /**
     * Method {@code eth_newFilter}.
     * @param req A request object containing the filter object of type {@link Types.FilterType}
     * @return the filter id
     */
    Message ethNewFilter(Types.RequestEthNewFilter req);
}