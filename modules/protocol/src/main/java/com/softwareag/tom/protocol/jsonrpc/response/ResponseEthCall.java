/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

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

    public ResponseEthCall(String ret) {
        super();
        this.result = new Result(ret);
    }

    public Types.ResponseEthCall getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthCall.newBuilder().setReturn(HexValue.toByteString(this.result.ret)).build();
        }
    }

    final static class Result {
        String ret;

        private Result() {}

        private Result(String ret) {
            this();
            this.ret = ret;
        }

        @Override public String toString() {
            return '"' + ret + '"';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;

            Result result = (Result) o;

            return ret.equals(result.ret);
        }

        @Override public int hashCode() {
            return ret.hashCode();
        }
    }
}