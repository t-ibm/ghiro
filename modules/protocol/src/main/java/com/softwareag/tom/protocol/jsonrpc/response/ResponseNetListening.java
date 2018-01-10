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
 * {@code net_listening}.
 */
public class ResponseNetListening extends Response<ResponseNetListening.Result, Types.ResponseNetListening> {

    public Types.ResponseNetListening getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseNetListening.newBuilder().setListening(this.result.listening).build();
        }
    }

    static class Result {
        @JsonProperty("listening") public boolean listening;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return listening == result.listening;
        }

        @Override public int hashCode() {
            return (listening ? 1 : 0);
        }
    }
}