/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;
import com.softwareag.tom.protocol.util.HexValue;

/**
 * {@code eth_newFilter}.
 */
public class ResponseEthNewFilter extends Response<ResponseEthNewFilter.Result, Types.ResponseEthNewFilter> {

    public ResponseEthNewFilter() {
        super();
    }

    public ResponseEthNewFilter(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthNewFilter(String filterId) {
        super();
        this.result = new Result(filterId);
    }

    public Types.ResponseEthNewFilter getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthNewFilter.newBuilder().setId(HexValue.toByteString(this.result.filterId)).build();
        }
    }

    public ByteString getFilterId() {
        return HexValue.toByteString(this.result.filterId);
    }

    final static class Result {
        @JsonProperty("sub_id") String filterId;

        private Result() {}

        private Result(String filterId) {
            this();
            this.filterId = filterId;
        }

        @Override public String toString() {
            return "{\"sub_id\":\"" + filterId + "\"}";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return filterId.equals(result.filterId);
        }

        @Override public int hashCode() {
            return filterId.hashCode();
        }
    }
}