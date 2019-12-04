package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.conf.Node;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.util.HexValue;

import java.io.IOException;
import java.util.Objects;

public class ParamsAddressDataTx extends ParamsAddressData {
    @JsonProperty("from") private String from;
    @JsonProperty("gasPrice") private long gasPrice;
    @JsonProperty("gas") private long gas;

    public ParamsAddressDataTx(String to, String data, long gasPrice, long gas) {
        super(to, data);
        setPrivKey();
        this.gasPrice = gasPrice;
        this.gas = gas;
    }

    ParamsAddressDataTx(Types.TxType tx) {
        super(tx);
        setPrivKey();
        this.gasPrice = HexValue.toBigInteger(tx.getGasPrice()).longValueExact();
        this.gas = HexValue.toBigInteger(tx.getGas()).longValueExact();
    }

    private void setPrivKey() {
        try {
            this.from = Node.instance().getKey().getPrivate(); //TODO :: Get this from burrow.toml or similar instead
        } catch (IOException e) {
            logger.error("Failed to retrieve private key.", e);
        }
    }

    @Override public String toString() {
        return "{\"to\":\"" + to + "\",\"data\":\"" + data + "\",\"from\":\"" + from + "\",\"gasPrice\":" + gasPrice + ",\"gas\":" + gas + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ParamsAddressDataTx that = (ParamsAddressDataTx) o;

        if (gasPrice != that.gasPrice) return false;
        if (gas != that.gas) return false;
        return Objects.equals(from, that.from);

    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (int) (gasPrice ^ (gasPrice >>> 32));
        result = 31 * result + (int) (gas ^ (gas >>> 32));
        return result;
    }
}
