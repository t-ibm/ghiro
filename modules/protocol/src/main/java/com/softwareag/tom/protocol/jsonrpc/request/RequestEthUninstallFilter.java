/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthUninstallFilter;
import com.softwareag.tom.protocol.util.HexValue;

import java.util.Objects;

/**
 * {@code eth_uninstallFilter}.
 */
public class RequestEthUninstallFilter extends Request<RequestEthUninstallFilter.Params, ResponseEthUninstallFilter> {
    public RequestEthUninstallFilter(Service jsonRpcService, Types.RequestEthUninstallFilter msg) {
        this(jsonRpcService, msg.getId());
    }

    public RequestEthUninstallFilter(Service jsonRpcService, ByteString filterId) {
        super(jsonRpcService, "burrow.eventUnsubscribe", new Params(filterId));
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
            return Objects.equals(filterId, params.filterId);
        }

        @Override public int hashCode() {
            return Objects.hash(filterId);
        }
    }
}