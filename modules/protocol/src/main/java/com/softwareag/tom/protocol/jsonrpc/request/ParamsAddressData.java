package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.util.HexValue;

public class ParamsAddressData extends ParamsAddress {
    @JsonProperty("data") String data;

    public ParamsAddressData(String address, String data) {
        super(address);
        this.data = data;
    }

    ParamsAddressData(Types.TxType tx) {
        super(tx.getTo());
        this.data = HexValue.stripPrefix(tx.getData());
    }

    @Override public String toString() {
        return "{\"address\":\"" + address + "\",\"data\":\"" + data + "\"}";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ParamsAddressData that = (ParamsAddressData) o;

        return data.equals(that.data);

    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }
}
