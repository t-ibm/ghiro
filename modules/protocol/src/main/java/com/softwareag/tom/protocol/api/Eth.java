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
     * @param request A request object containing the address to check for balance as well as a parameter of type {@link Types.BlockHeightType}
     * @return the integer of the current balance in wei
     * @throws IOException in case of an error
     */
    Types.ResponseEthGetBalance ethGetBalance(Types.RequestEthGetBalance request) throws IOException;

    /**
     * Method {@code eth_getStorageAt}.
     * @param request A request object containing the address and position in storage as well as a parameter of type {@link Types.BlockHeightType}
     * @return the value from a storage position at a given address
     * @throws IOException in case of an error
     */
    Types.ResponseEthGetStorageAt ethGetStorageAt(Types.RequestEthGetStorageAt request) throws IOException;

    /**
     * Method {@code eth_sendTransaction}.
     * @param request A request object containing the transaction object of type {@link Types.TxType}
     * @return the transaction hash, or the zero hash if the transaction is not yet available
     * @throws IOException in case of an error
     */
    Types.ResponseEthSendTransaction ethSendTransaction(Types.RequestEthSendTransaction request) throws IOException;

    /**
     * Method {@code eth_call}.
     * @param request A request object containing the transaction object of type {@link Types.TxType} as well as a parameter of type {@link Types.BlockHeightType}
     * @return the return value of executed contract
     * @throws IOException in case of an error
     */
    Types.ResponseEthCall ethCall(Types.RequestEthCall request) throws IOException;

    /**
     * Method {@code eth_newFilter}.
     * @param request A request object containing the filter object of type {@link Types.FilterOptionType}
     * @return the filter id
     * @throws IOException in case of an error
     */
    Types.ResponseEthNewFilter ethNewFilter(Types.RequestEthNewFilter request) throws IOException;

    /**
     * Method {@code eth_newBlockFilter}.
     * @return the filter id
     * @throws IOException in case of an error
     */
    Types.ResponseEthNewFilter ethNewBlockFilter() throws IOException;

    /**
     * Method {@code eth_uninstallFilter}.
     * @param request The filter id
     * @return {@code true} if the filter was successfully uninstalled, {@code false} otherwise
     * @throws IOException in case of an error
     */
    Types.ResponseEthUninstallFilter ethUninstallFilter(Types.RequestEthUninstallFilter request) throws IOException;

    /**
     * Method {@code eth_getFilterChanges}.
     * @param request A request object containing the filter id
     * @return a list of objects of type {@link Types.FilterLogType}
     * @throws IOException in case of an error
     */
    Types.ResponseEthGetFilterChanges ethGetFilterChanges(Types.RequestEthGetFilterChanges request) throws IOException;

    /**
     * Method {@code eth_getTransactionReceipt}.
     * @param request A request object containing the hash of a transaction
     * @return an object of type {@link Types.TxReceiptType}
     * @throws IOException in case of an error
     */
    Types.ResponseEthGetTransactionReceipt ethGetTransactionReceipt(Types.RequestEthGetTransactionReceipt request) throws IOException;
}