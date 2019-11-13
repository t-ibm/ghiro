package com.softwareag.tom.protocol.jsonrpc.request;

import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.util.HexValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParamsAddress extends AbstractList<String> implements List<String> {
    private static final Logger logger = LoggerFactory.getLogger(ParamsAddress.class);

    private List<String> address = new ArrayList<>();

    public ParamsAddress(String address, String defaultBlock) {
        this.address.add(address);
        this.address.add(defaultBlock);
    }

    ParamsAddress(ByteString address, String defaultBlock) {
        this(validate(address), defaultBlock);
    }

    static String validate(ByteString immutableByteArray) {
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

    @Override public int size() {
        return address.size();
    }

    @Override public String get(int i) {
        return address.get(i);
    }

    @Override public String toString() {
        return '[' + address.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + ']';
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