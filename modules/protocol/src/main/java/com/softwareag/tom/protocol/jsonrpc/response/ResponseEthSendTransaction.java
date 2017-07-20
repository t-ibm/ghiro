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
import com.softwareag.tom.protocol.util.HexValue;

/**
 * {@code eth_sendTransaction}.
 */
public class ResponseEthSendTransaction extends Response<ResponseEthSendTransaction.Result> {

    public Message getResponse() {
        if (this.error != null) {
            return Types.ResponseException.newBuilder().setCode(Types.CodeType.InternalError).setMessage(this.error.message).build();
        } else {
            return Types.ResponseEthSendTransaction.newBuilder().setHash(HexValue.toByteString(this.result.txId)).build();
        }
    }

    static class Result {
        @JsonProperty("tx_id") public String txId;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return txId != null ? txId.equals(result.txId) : result.txId == null;
        }

        @Override public int hashCode() {
            return txId != null ? txId.hashCode() : 0;
        }
    }
}