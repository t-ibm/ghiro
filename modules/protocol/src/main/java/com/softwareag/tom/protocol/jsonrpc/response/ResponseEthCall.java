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
import com.softwareag.tom.protocol.util.HexValue;

/**
 * {@code eth_call}.
 */
public class ResponseEthCall extends Response<ResponseEthCall.Result, Types.ResponseEthCall> {

    public ResponseEthCall() {
        super();
    }

    public ResponseEthCall(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthCall(long gasUsed, String ret) {
        super();
        this.result = new Result(gasUsed, ret);
    }

    public Types.ResponseEthCall getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthCall.newBuilder().setReturn(HexValue.toByteString(this.result.ret)).build();
        }
    }

    final static class Result {
        @JsonProperty("gas_used") long gasUsed;
        @JsonProperty("return") String ret;

        private Result() {}

        private Result(long gasUsed, String ret) {
            this();
            this.gasUsed = gasUsed;
            this.ret = ret;
        }

        @Override public String toString() {
            return "{\"gas_used\":" + gasUsed + ", \"return\":\"" + ret + "\"}";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            if (gasUsed != result.gasUsed) return false;
            return ret.equals(result.ret);
        }

        @Override public int hashCode() {
            int result = (int) (gasUsed ^ (gasUsed >>> 32));
            result = 31 * result + ret.hashCode();
            return result;
        }
    }
}