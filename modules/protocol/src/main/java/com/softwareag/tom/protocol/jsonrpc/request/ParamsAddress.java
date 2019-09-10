package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.util.HexValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamsAddress {
    static final Logger logger = LoggerFactory.getLogger(ParamsAddress.class);

    @JsonProperty("address") String address;

    public ParamsAddress(String address) {
        this.address = address;
    }

    ParamsAddress(ByteString address) {
        this.address = validate(address);
    }

    private static String validate(ByteString immutableByteArray) {
        if (immutableByteArray == null || immutableByteArray.size() == 0) {
            return "";
        } else if (immutableByteArray.size() != 20 * 2 + 2) {
            logger.warn("Address size is {} bytes while it should be 20.", immutableByteArray.size() / 2 - 2);
        } else if (!immutableByteArray.isValidUtf8()) {
            logger.warn("Address is not a valid UTF-8 encoded string.");
        } else {
            return HexValue.stripPrefix(immutableByteArray);
        }
        return "";
    }

    @Override public String toString() {
        return "{\"address\":\"" + address + "\"}";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParamsAddress that = (ParamsAddress) o;

        return address.equals(that.address);

    }

    @Override public int hashCode() {
        return address.hashCode();
    }
}