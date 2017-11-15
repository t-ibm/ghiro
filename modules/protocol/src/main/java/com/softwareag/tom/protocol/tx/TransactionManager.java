/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.tx;

import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.abi.Types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transaction manager implementation using Burrow's unsafe API to manage the remotely created and signed transactions.
 */
public class TransactionManager {
    public static final TransactionManager instance = new TransactionManager();

    private final Map<ByteString, Types.TxReceiptType> transactions = new ConcurrentHashMap<>();

    private TransactionManager() {}

    public Types.TxReceiptType getTransactionReceipt(ByteString txHash) {
        if (transactions.containsKey(txHash)) {
            return transactions.get(txHash);
        }
        return null;
    }

    public void setTransactionReceipt(Types.TxReceiptType transactionReceipt) {
        this.transactions.put(transactionReceipt.getTransactionHash(), transactionReceipt);
    }
}