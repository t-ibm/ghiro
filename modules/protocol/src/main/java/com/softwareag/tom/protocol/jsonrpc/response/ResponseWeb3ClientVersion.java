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

/**
 * {@code web3_clientVersion}.
 */
public class ResponseWeb3ClientVersion extends Response<ResponseWeb3ClientVersion.Result, Types.ResponseWeb3ClientVersion> {

    public ResponseWeb3ClientVersion() {
        super();
    }

    public ResponseWeb3ClientVersion(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseWeb3ClientVersion(String clientVersion) {
        super();
        this.result = new Result(clientVersion);
    }

    public Types.ResponseWeb3ClientVersion getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseWeb3ClientVersion.newBuilder().setVersion(this.result.clientVersion).build();
        }
    }

    final static class Result {
        @JsonProperty("client_version") String clientVersion;

        private Result() {}

        private Result(String clientVersion) {
            this();
            this.clientVersion = clientVersion;
        }

        @Override public String toString() {
            return '"' + clientVersion + '"';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return clientVersion.equals(result.clientVersion);
        }

        @Override public int hashCode() {
            return clientVersion.hashCode();
        }
    }
}