/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.Message;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;
import com.softwareag.tom.protocol.tx.TransactionManager;
import com.softwareag.tom.protocol.util.HexValue;

/**
 * {@code eth_sendTransaction}.
 */
public class ResponseEthSendTransaction extends Response<ResponseEthSendTransaction.Result> {

    public Message getResponse() {
        if (this.error != null) {
            return Types.ResponseException.newBuilder().setCode(Types.CodeType.InternalError).setMessage(this.error.message).build();
        } else {
            TransactionManager.instance.setTransactionReceipt(Types.TxReceiptType.newBuilder().setTransactionHash(HexValue.toByteString(this.result.txId)).setContractAddress(HexValue.toByteString(this.result.callData.contractAddress)).build());
            return Types.ResponseEthSendTransaction.newBuilder().setHash(HexValue.toByteString(this.result.txId)).build();
        }
    }

    static class Result {
        @JsonProperty("tx_id") public String txId;
        @JsonProperty("call_data") public CallData callData;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            if (txId != null ? !txId.equals(result.txId) : result.txId != null) return false;
            return callData != null ? callData.equals(result.callData) : result.callData == null;
        }

        @Override public int hashCode() {
            int result = txId != null ? txId.hashCode() : 0;
            result = 31 * result + (callData != null ? callData.hashCode() : 0);
            return result;
        }
    }

    private static class CallData {
        @JsonProperty("callee") public String contractAddress;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CallData callData = (CallData) o;

            return contractAddress != null ? contractAddress.equals(callData.contractAddress) : callData.contractAddress == null;
        }

        @Override public int hashCode() {
            return contractAddress != null ? contractAddress.hashCode() : 0;
        }
    }
}