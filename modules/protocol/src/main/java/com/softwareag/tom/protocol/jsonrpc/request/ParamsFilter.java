package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.util.HexValue;

public class ParamsFilter {
    @JsonProperty("sub_id") private String filterId;

    public ParamsFilter(String filterId) {
        this.filterId = filterId;
    }

    ParamsFilter(ByteString filterId) {
        this.filterId = HexValue.stripPrefix(filterId);
    }

    @Override public String toString() {
        return "{\"sub_id\":\"" + filterId + "\"}";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParamsFilter paramsSub = (ParamsFilter) o;

        return filterId.equals(paramsSub.filterId);
    }

    @Override public int hashCode() {
        return filterId.hashCode();
    }
}
