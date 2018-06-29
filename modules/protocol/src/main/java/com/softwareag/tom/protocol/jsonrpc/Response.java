/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC base response implementation.
 * @param <T> The expected result type of this response
 * @param <M> The expected protocol buffer message type of this response
 */
public abstract class Response<T, M> {
    @JsonProperty("jsonrpc") protected String jsonrpc;
    @JsonProperty("result") protected T result;
    @JsonProperty("error") protected Error error;
    @JsonProperty("id") protected long id;

    public boolean hasError() {
        return error != null;
    }
    public Error getError() {
        return error;
    }

    public abstract M getResponse();

    public static class Error {
        @JsonProperty("code") public int code;
        @JsonProperty("message") public String message;
        @JsonProperty("data") public String data;

        @Override public int hashCode() {
            int result = code;
            result = 31 * result + (message != null ? message.hashCode() : 0);
            result = 31 * result + (data != null ? data.hashCode() : 0);
            return result;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Error error = (Error) o;

            if (code != error.code) return false;
            if (message != null ? !message.equals(error.message) : error.message != null) return false;
            return data != null ? data.equals(error.data) : error.data == null;
        }
    }

    @Override public int hashCode() {
        int result1 = jsonrpc != null ? jsonrpc.hashCode() : 0;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (error != null ? error.hashCode() : 0);
        result1 = 31 * result1 + (int) (id ^ (id >>> 32));
        return result1;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Response<?, ?> response = (Response<?, ?>) o;

        if (id != response.id) return false;
        if (jsonrpc != null ? !jsonrpc.equals(response.jsonrpc) : response.jsonrpc != null) return false;
        if (result != null ? !result.equals(response.result) : response.result != null) return false;
        return error != null ? error.equals(response.error) : response.error == null;
    }
}