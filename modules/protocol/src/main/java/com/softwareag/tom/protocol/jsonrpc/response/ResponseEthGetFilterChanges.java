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
import com.softwareag.tom.protocol.util.HexValue;

import java.util.List;

/**
 * {@code eth_getFilterChanges}.
 */
public class ResponseEthGetFilterChanges extends Response<ResponseEthGetFilterChanges.Result, Types.ResponseEthGetFilterChanges> {

    public Types.ResponseEthGetFilterChanges getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            Types.ResponseEthGetFilterChanges.Builder builder = Types.ResponseEthGetFilterChanges.newBuilder();
            for (int i = 0; i < this.result.events.size(); i++) {
                Event event = this.result.events.get(i);
                builder.addLog(i, Types.FilterLogType.newBuilder().setAddress(HexValue.toByteString(event.address)).setData(HexValue.toByteString(event.data)).build()); //TODO :: height, topics
            }
            return builder.build();
        }
    }

    static class Result {
        @JsonProperty("events") public List<Event> events;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return events != null ? events.equals(result.events) : result.events == null;
        }

        @Override public int hashCode() {
            return events != null ? events.hashCode() : 0;
        }
    }

    private static class Event {
        @JsonProperty("address") public String address;
        @JsonProperty("data") public String data;
        @JsonProperty("height") public long height;
        @JsonProperty("topics") public List<String> topics;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Event event = (Event) o;

            if (height != event.height) return false;
            if (address != null ? !address.equals(event.address) : event.address != null) return false;
            if (data != null ? !data.equals(event.data) : event.data != null) return false;
            return topics != null ? topics.equals(event.topics) : event.topics == null;
        }

        @Override public int hashCode() {
            int result = address != null ? address.hashCode() : 0;
            result = 31 * result + (data != null ? data.hashCode() : 0);
            result = 31 * result + (int) (height ^ (height >>> 32));
            result = 31 * result + (topics != null ? topics.hashCode() : 0);
            return result;
        }
    }
}