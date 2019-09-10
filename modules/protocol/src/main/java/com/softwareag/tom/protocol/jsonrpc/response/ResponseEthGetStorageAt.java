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
 * {@code eth_getStorageAt}.
 */
public class ResponseEthGetStorageAt extends Response<ResponseEthGetStorageAt.Result, Types.ResponseEthGetStorageAt> {

    public ResponseEthGetStorageAt() {
        super();
    }

    public ResponseEthGetStorageAt(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthGetStorageAt(String key, String value) {
        super();
        this.result = new Result(key, value);
    }

    public Types.ResponseEthGetStorageAt getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthGetStorageAt.newBuilder().setValue(HexValue.toByteString(this.result.value)).build();
        }
    }

    final static class Result {
        @JsonProperty("key") String key;
        @JsonProperty("value") String value;

        private Result() {}

        private Result(String key, String value) {
            this();
            this.key = key;
            this.value = value;
        }

        @Override public String toString() {
            return "{\"key\":\"" + key + "\",\"value\":\"" + value + "\"}";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            if (!key.equals(result.key)) return false;
            return value.equals(result.value);
        }

        @Override public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }
    }
}