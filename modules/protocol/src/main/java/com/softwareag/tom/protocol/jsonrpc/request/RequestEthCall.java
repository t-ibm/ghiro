/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthCall;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthSendTransaction;

/**
 * {@code eth_call}.
 */
public class RequestEthCall extends Request<RequestEthCall.Params, ResponseEthCall> {
    public RequestEthCall(Service jsonRpcService, Types.RequestEthCall msg) {
        super(jsonRpcService, "burrow.call", new Params(msg.getTx()));
    }

    static class Params {
        @JsonProperty("address") public String address;
        @JsonProperty("data") public String data;

        Params(Types.TxType tx) {
            this.address = validate(tx.getTo());
            this.data = tx.getData().toStringUtf8();
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Params params = (Params) o;

            if (address != null ? !address.equals(params.address) : params.address != null) return false;
            return data != null ? data.equals(params.data) : params.data == null;
        }

        @Override public int hashCode() {
            int result = address != null ? address.hashCode() : 0;
            result = 31 * result + (data != null ? data.hashCode() : 0);
            return result;
        }
    }
}