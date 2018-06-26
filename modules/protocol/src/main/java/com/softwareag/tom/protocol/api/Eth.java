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
 * The eth portion of the Ethereum JSON-RPC API. See the <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC">JSON-RPC Wiki</a> for more info.
 */
interface Eth {
    /**
     * Method {@code eth_getBalance}.
     * @param req A request object containing the address to check for balance as well as a parameter of type {@link Types.BlockHeightType}
     * @return the integer of the current balance in wei
     */
    Types.ResponseEthGetBalance ethGetBalance(Types.RequestEthGetBalance req) throws IOException;

    /**
     * Method {@code eth_getStorageAt}.
     * @param req A request object containing the address and position in storage as well as a parameter of type {@link Types.BlockHeightType}
     * @return the value from a storage position at a given address
     */
    Types.ResponseEthGetStorageAt ethGetStorageAt(Types.RequestEthGetStorageAt req) throws IOException;

    /**
     * Method {@code eth_sendTransaction}.
     * @param req A request object containing the transaction object of type {@link Types.TxType}
     * @return the transaction hash, or the zero hash if the transaction is not yet available
     */
    Types.ResponseEthSendTransaction ethSendTransaction(Types.RequestEthSendTransaction req) throws IOException;

    /**
     * Method {@code eth_call}.
     * @param req A request object containing the transaction object of type {@link Types.TxType} as well as a parameter of type {@link Types.BlockHeightType}
     * @return the return value of executed contract
     */
    Types.ResponseEthCall ethCall(Types.RequestEthCall req) throws IOException;

    /**
     * Method {@code eth_newFilter}.
     * @param req A request object containing the filter object of type {@link Types.FilterOptionType}
     * @return the filter id
     */
    Types.ResponseEthNewFilter ethNewFilter(Types.RequestEthNewFilter req) throws IOException;

    /**
     * Method {@code eth_newBlockFilter}.
     * @return the filter id
     */
    Types.ResponseEthNewFilter ethNewBlockFilter() throws IOException;

    /**
     * Method {@code eth_uninstallFilter}.
     * @param req The filter id
     * @return {@code true} if the filter was successfully uninstalled, {@code false} otherwise
     */
    Types.ResponseEthUninstallFilter ethUninstallFilter(Types.RequestEthUninstallFilter req) throws IOException;

    /**
     * Method {@code eth_getFilterChanges}.
     * @param req A request object containing the filter id
     * @return a list of objects of type {@link Types.FilterLogType}
     */
    Types.ResponseEthGetFilterChanges ethGetFilterChanges(Types.RequestEthGetFilterChanges req) throws IOException;

    /**
     * Method {@code eth_getTransactionReceipt}.
     * @param req A request object containing the hash of a transaction
     * @return an object of type {@link Types.TxReceiptType}
     */
    Types.ResponseEthGetTransactionReceipt ethGetTransactionReceipt(Types.RequestEthGetTransactionReceipt req);
}