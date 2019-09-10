/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;

/**
 * {@code eth_uninstallFilter}.
 */
public class ResponseEthUninstallFilter extends Response<ResponseEthUninstallFilter.Result, Types.ResponseEthUninstallFilter> {

    public ResponseEthUninstallFilter() {
        super();
    }

    public ResponseEthUninstallFilter(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthUninstallFilter(boolean isRemoved) {
        super();
        this.result = new Result(isRemoved);
    }

    public Types.ResponseEthUninstallFilter getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthUninstallFilter.newBuilder().setRemoved(this.result.isRemoved).build();
        }
    }

    public boolean isRemoved() { return this.result.isRemoved; }

    final static class Result {
        @JsonProperty("result") boolean isRemoved;

        private Result() {}

        private Result(boolean isRemoved) {
            this();
            this.isRemoved = isRemoved;
        }

        @Override public String toString() {
            return "{\"result\":\"" + isRemoved + "\"}";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return isRemoved == result.isRemoved;
        }

        @Override public int hashCode() {
            return (isRemoved ? 1 : 0);
        }
    }
}