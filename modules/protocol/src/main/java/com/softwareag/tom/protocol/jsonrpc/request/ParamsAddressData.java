package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.util.HexValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamsAddressData {
    static final Logger logger = LoggerFactory.getLogger(ParamsAddressData.class);

    @JsonProperty("to") String to;
    @JsonProperty("data") String data;

    public ParamsAddressData(String address, String data) {
        this.to = address;
        this.data = data;
    }

    ParamsAddressData(Types.TxType tx) {
        this.to = ParamsAddress.validate(tx.getTo());
        this.data = HexValue.stripPrefix(tx.getData());
    }

    @Override public String toString() {
        return "{\"to\":\"" + to + "\",\"data\":\"" + data + "\"}";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParamsAddressData)) return false;

        ParamsAddressData that = (ParamsAddressData) o;

        if (!to.equals(that.to)) return false;
        return data.equals(that.data);
    }

    @Override public int hashCode() {
        int result = to.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }
}
