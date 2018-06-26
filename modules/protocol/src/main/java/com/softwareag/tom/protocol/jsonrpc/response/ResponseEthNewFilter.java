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

    static class Result {
        @JsonProperty("sub_id") public String filterId;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return filterId != null ? filterId.equals(result.filterId) : result.filterId == null;
        }

        @Override public int hashCode() {
            return filterId != null ? filterId.hashCode() : 0;
        }
    }
}