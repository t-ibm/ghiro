/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;
import com.softwareag.tom.protocol.util.HexValue;

/**
 * {@code eth_getTransactionReceipt}.
 */
public class ResponseEthGetTransactionReceipt extends Response<ResponseEthGetTransactionReceipt.Result, Types.ResponseEthGetTransactionReceipt> {

    public ResponseEthGetTransactionReceipt() {
        super();
    }

    public ResponseEthGetTransactionReceipt(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthGetTransactionReceipt(String txHash, String txIndex, String from, String to, long gasUsed) {
        super();
        this.result = new Result(txHash, txIndex, from, to, gasUsed);
    }

    public Types.ResponseEthGetTransactionReceipt getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthGetTransactionReceipt.newBuilder().setTxReceipt(
                Types.TxReceiptType.newBuilder().setTransactionHash(HexValue.toByteString(this.result.txHash)).setTransactionIndex(HexValue.toByteString(this.result.txIndex)).build()
            ).build();
        }
    }

    final static class Result {
        @JsonProperty("transactionHash") String txHash;
        @JsonProperty("transactionIndex") String txIndex;
        @JsonProperty("from") String from;
        @JsonProperty("to") String to;
        @JsonProperty("gasUsed") long gasUsed;

        private Result() {}

        private Result(String txHash, String txIndex, String from, String to, long gasUsed) {
            this();
            this.txHash = txHash;
            this.txIndex = txIndex;
            this.from = from;
            this.to = to;
            this.gasUsed = gasUsed;
        }

        @Override public String toString() {
            return "{" + "\"transactionHash\":\""  + txHash + "\"" + ",\"transactionIndex\":\""  + txIndex + "\"" + ",\"from\":\""  + from + "\"" +  ",\"to\":\""  + to + "\"" + ",\"gasUsed\":\""  + gasUsed + "\"" + "}";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;

            Result result = (Result) o;

            if (gasUsed != result.gasUsed) return false;
            if (!txHash.equals(result.txHash)) return false;
            if (!txIndex.equals(result.txIndex)) return false;
            if (!from.equals(result.from)) return false;
            return to.equals(result.to);
        }

        @Override public int hashCode() {
            int result = txHash.hashCode();
            result = 31 * result + txIndex.hashCode();
            result = 31 * result + from.hashCode();
            result = 31 * result + to.hashCode();
            result = 31 * result + (int) (gasUsed ^ (gasUsed >>> 32));
            return result;
        }
    }
}