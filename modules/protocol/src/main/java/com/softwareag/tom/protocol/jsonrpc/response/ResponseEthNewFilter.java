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
 * {@code eth_newFilter}.
 */
public class ResponseEthNewFilter extends Response<ResponseEthNewFilter.Result> {

    public Message getResponse() {
        if (this.error != null) {
            return Types.ResponseException.newBuilder().setCode(Types.CodeType.InternalError).setMessage(this.error.message).build();
        } else {
            return Types.ResponseEthNewFilter.newBuilder().setFilterId(ByteString.copyFromUtf8(this.result.subId)).build();
        }
    }

    static class Result {
        @JsonProperty("sub_id") public String subId;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return subId != null ? subId.equals(result.subId) : result.subId == null;
        }

        @Override public int hashCode() {
            return subId != null ? subId.hashCode() : 0;
        }
    }
}