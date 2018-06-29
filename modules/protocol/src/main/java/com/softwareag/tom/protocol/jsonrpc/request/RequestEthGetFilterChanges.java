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
import com.softwareag.tom.protocol.util.HexValue;

/**
 * {@code eth_getFilterChanges}.
 */
public class RequestEthGetFilterChanges extends Request<RequestEthGetFilterChanges.Params, ResponseEthGetFilterChanges> {
    public RequestEthGetFilterChanges(Service jsonRpcService, Types.RequestEthGetFilterChanges msg) {
        this(jsonRpcService, msg.getId());
    }

    public RequestEthGetFilterChanges(Service jsonRpcService, ByteString filterId) {
        super(jsonRpcService, "burrow.eventPoll", new Params(filterId));
    }

    static class Params {
        @JsonProperty("sub_id") public String filterId;

        Params(ByteString filterId) {
            this.filterId = HexValue.stripPrefix(filterId);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Params params = (Params) o;
            return filterId != null ? filterId.equals(params.filterId) : params.filterId == null;
        }

        @Override public int hashCode() {
            return filterId != null ? filterId.hashCode() : 0;
        }
    }
}