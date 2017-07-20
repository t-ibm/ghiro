/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetFilterChanges;

/**
 * {@code eth_newFilter}.
 */
public class RequestEthGetFilterChanges extends Request<RequestEthGetFilterChanges.Params, ResponseEthGetFilterChanges> {
    public RequestEthGetFilterChanges(Service jsonRpcService, Types.RequestEthGetFilterChanges msg) {
        super(jsonRpcService, "burrow.eventPoll", new Params(msg.getId()));
    }

    static class Params {
        @JsonProperty("sub_id") public String subId;

        Params(ByteString filterId) {
            this.subId = filterId.toStringUtf8();
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Params params = (Params) o;

            return subId != null ? subId.equals(params.subId) : params.subId == null;
        }

        @Override public int hashCode() {
            return subId != null ? subId.hashCode() : 0;
        }
    }
}