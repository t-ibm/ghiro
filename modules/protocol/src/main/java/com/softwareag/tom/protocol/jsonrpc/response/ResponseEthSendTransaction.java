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

    public ResponseEthSendTransaction(String contractAddress, String userAddress, String data, String txId, long gasUsed) {
        super();
        this.result = new Result(contractAddress, userAddress, data, txId, gasUsed);
    }

    public Types.ResponseEthSendTransaction getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            TransactionManager.instance.setTransactionReceipt(Types.TxReceiptType.newBuilder().setTransactionHash(HexValue.toByteString(this.result.txId)).setContractAddress(HexValue.toByteString(this.result.callData.contractAddress)).build());
            return Types.ResponseEthSendTransaction.newBuilder().setHash(HexValue.toByteString(this.result.txId)).build();
        }
    }

    final static class Result {
        @JsonProperty("call_data") CallData callData;
        @JsonProperty("exception") String exception;
        @JsonProperty("origin") String origin;
        @JsonProperty("return") String ret;
        @JsonProperty("tx_id") String txId;

        private Result() {}

        private Result(String contractAddress, String userAddress, String data, String txId, long gasUsed) {
            this();
            this.callData = new CallData(contractAddress, userAddress, data, gasUsed);
            this.exception = "";
            this.origin = userAddress;
            this.ret = data;
            this.txId = txId;
        }

        @Override public String toString() {
            return "{\"call_data\":" + callData + ", \"exception\":\"" + exception + "\", \"origin\":\"" + origin + "\", \"return\":\"" + ret + "\", \"tx_id\":\"" + txId + "\"}";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            if (!callData.equals(result.callData)) return false;
            if (!exception.equals(result.exception)) return false;
            if (!origin.equals(result.origin)) return false;
            if (!ret.equals(result.ret)) return false;
            return txId.equals(result.txId);
        }

        @Override public int hashCode() {
            int result = callData.hashCode();
            result = 31 * result + exception.hashCode();
            result = 31 * result + origin.hashCode();
            result = 31 * result + ret.hashCode();
            result = 31 * result + txId.hashCode();
            return result;
        }
    }

    final private static class CallData {
        @JsonProperty("callee") String contractAddress;
        @JsonProperty("caller") String userAddress;
        @JsonProperty("data") String data;
        @JsonProperty("gas") long gasUsed;
        @JsonProperty("value") long value;

        private CallData() {}

        private CallData(String contractAddress, String userAddress, String data, long gasUsed) {
            this();
            this.contractAddress = contractAddress;
            this.userAddress = userAddress;
            this.data = data;
            this.gasUsed = gasUsed;
        }

        @Override public String toString() {
            return "{\"callee\":\"" + contractAddress + "\", \"caller\":\"" + userAddress + "\", \"data\":\"" + data + "\", \"gas\":" + gasUsed + ", \"value\":" + value + '}';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CallData callData = (CallData) o;

            if (gasUsed != callData.gasUsed) return false;
            if (value != callData.value) return false;
            if (!contractAddress.equals(callData.contractAddress)) return false;
            if (!userAddress.equals(callData.userAddress)) return false;
            return data.equals(callData.data);
        }

        @Override public int hashCode() {
            int result = contractAddress.hashCode();
            result = 31 * result + userAddress.hashCode();
            result = 31 * result + data.hashCode();
            result = 31 * result + (int) (gasUsed ^ (gasUsed >>> 32));
            result = 31 * result + (int) (value ^ (value >>> 32));
            return result;
        }
    }
}