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
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetStorageAt;

/**
 * {@code eth_getStorageAt}.
 */
public class RequestEthGetStorageAt extends Request<RequestEthGetStorageAt.Params, ResponseEthGetStorageAt> {
    public RequestEthGetStorageAt(Service jsonRpcService, Types.RequestEthGetStorageAt msg) {
        super(jsonRpcService, "burrow.getStorageAt", new Params(msg.getAddress()));
    }

    static class Params {
        @JsonProperty("address") public String address;

        Params(ByteString address) {
            this.address = validate(address);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Params params = (Params) o;

            return address != null ? address.equals(params.address) : params.address == null;
        }

        @Override public int hashCode() {
            return address != null ? address.hashCode() : 0;
        }
    }
}