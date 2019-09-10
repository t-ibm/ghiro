package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.util.HexValue;

public class ParamsEvent {
    @JsonProperty("event_id") private String eventId;

    ParamsEvent(ByteString address) {
        this.eventId = "Log/" + HexValue.stripPrefix(address);
    }

    public ParamsEvent(String eventId) {
        this.eventId = eventId;
    }

    @Override public String toString() {
        return "{\"event_id\":\"" + eventId + "\"}";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParamsEvent that = (ParamsEvent) o;

        return eventId.equals(that.eventId);
    }

    @Override public int hashCode() {
        return eventId.hashCode();
    }
}
