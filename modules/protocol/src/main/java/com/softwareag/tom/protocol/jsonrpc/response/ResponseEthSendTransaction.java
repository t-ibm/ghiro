/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;
import com.softwareag.tom.protocol.tx.TransactionManager;
import com.softwareag.tom.protocol.util.HexValue;

/**
 * {@code eth_sendTransaction}.
 */
public class ResponseEthSendTransaction extends Response<ResponseEthSendTransaction.Result, Types.ResponseEthSendTransaction> {

    public ResponseEthSendTransaction() {
        super();
    }

    public ResponseEthSendTransaction(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthSendTransaction(String txId) {
        super();
        this.result = new Result(txId);
    }

    public Types.ResponseEthSendTransaction getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            TransactionManager.instance.setTransactionReceipt(Types.TxReceiptType.newBuilder().setTransactionHash(HexValue.toByteString(this.result.txId)).build());
            return Types.ResponseEthSendTransaction.newBuilder().setHash(HexValue.toByteString(this.result.txId)).build();
        }
    }

    final static class Result {
        @JsonProperty("transactionIndex") String txId;

        private Result() {}

        private Result(String txId) {
            this();
            this.txId = txId;
        }

        @Override public String toString() {
            return '"' + txId + '"';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;

            Result result = (Result) o;

            return txId.equals(result.txId);
        }

        @Override public int hashCode() {
            return txId.hashCode();
        }
    }
}