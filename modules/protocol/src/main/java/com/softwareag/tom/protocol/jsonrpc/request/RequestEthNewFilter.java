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
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter;

/**
 * {@code eth_newFilter}.
 */
public class RequestEthNewFilter extends Request<RequestEthNewFilter.Params, ResponseEthNewFilter> {
    public RequestEthNewFilter(Service jsonRpcService, Types.RequestEthNewFilter msg) {
        super(jsonRpcService, "burrow.eventSubscribe", new Params(msg.getOptions()));
    }

    static class Params {
        @JsonProperty("event_id") public String eventId;

        Params(Types.FilterType options) {
            this.eventId = "Log/" + options.getAddress().toStringUtf8();
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Params params = (Params) o;

            return eventId != null ? eventId.equals(params.eventId) : params.eventId == null;
        }

        @Override public int hashCode() {
            return eventId != null ? eventId.hashCode() : 0;
        }
    }
}