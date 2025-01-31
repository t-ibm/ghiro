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
 * {@code eth_getStorageAt}.
 */
public class ResponseEthGetStorageAt extends Response<ResponseEthGetStorageAt.Result, Types.ResponseEthGetStorageAt> {

    public ResponseEthGetStorageAt() {
        super();
    }

    public ResponseEthGetStorageAt(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthGetStorageAt(String storage) {
        super();
        this.result = new Result(storage);
    }

    public Types.ResponseEthGetStorageAt getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthGetStorageAt.newBuilder().setValue(HexValue.toByteString(this.result.storage)).build();
        }
    }

    final static class Result {
        String storage;

        private Result() {}

        private Result(String storage) {
            this();
            this.storage = storage;
        }

        @Override public String toString() {
            return '"' + storage + '"';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;

            Result result = (Result) o;

            return storage.equals(result.storage);
        }

        @Override public int hashCode() {
            return storage.hashCode();
        }
    }
}