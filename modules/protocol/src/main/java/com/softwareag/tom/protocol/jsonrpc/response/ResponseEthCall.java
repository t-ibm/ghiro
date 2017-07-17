/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;

/**
 * {@code eth_call}.
 */
public class ResponseEthCall extends Response<ResponseEthCall.Result> {

    public Message getResponse() {
        if (this.error != null) {
            return Types.ResponseException.newBuilder().setCode(Types.CodeType.InternalError).setMessage(this.error.message).build();
        } else {
            return Types.ResponseEthCall.newBuilder().setReturn(ByteString.copyFromUtf8(this.result.ret)).build();
        }
    }

    static class Result {
        @JsonProperty("return") public String ret;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return ret != null ? ret.equals(result.ret) : result.ret == null;
        }

        @Override public int hashCode() {
            return ret != null ? ret.hashCode() : 0;
        }
    }
}