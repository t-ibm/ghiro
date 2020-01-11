package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.conf.Node;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.util.HexValue;

import java.io.IOException;
import java.math.BigInteger;

public class ParamsAddressDataTx extends ParamsAddressData {
    @JsonProperty("from") private String from;
    @JsonProperty("gasPrice") private String gasPrice;
    @JsonProperty("gas") private String gas;

    public ParamsAddressDataTx(String to, String data, long value, long gasPrice, long gas) {
        super(to, data, value);
        setPrivKey();
        this.gasPrice = HexValue.toString(BigInteger.valueOf(gasPrice));
        this.gas = HexValue.toString(BigInteger.valueOf(gas));
    }

    ParamsAddressDataTx(Types.TxType tx) {
        super(tx);
        setPrivKey();
        this.gasPrice = HexValue.toString(tx.getGasPrice());
        this.gas = HexValue.toString(tx.getGas());
    }

    private void setPrivKey() {
        try {
            this.from = Node.instance().getKey().getPrivate(); //TODO :: Get this from burrow.toml or similar instead
        } catch (IOException e) {
            logger.error("Failed to retrieve private key.", e);
        }
    }

    @Override public String toString() {
        return "{\"to\":\"" + to + "\",\"data\":\"" + data + "\",\"value\":\"" + value + "\",\"from\":\"" + from + "\",\"gasPrice\":\"" + gasPrice + "\",\"gas\":\"" + gas + "\"}";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!o.getClass().equals(this.getClass())) return false;
        if (!super.equals(o)) return false;

        ParamsAddressDataTx that = (ParamsAddressDataTx) o;

        if (!from.equals(that.from)) return false;
        if (!gasPrice.equals(that.gasPrice)) return false;
        return gas.equals(that.gas);

    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + from.hashCode();
        result = 31 * result + gasPrice.hashCode();
        result = 31 * result + gas.hashCode();
        return result;
    }
}
