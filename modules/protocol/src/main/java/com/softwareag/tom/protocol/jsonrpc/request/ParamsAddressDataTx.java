package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.conf.Node;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.util.HexValue;

import java.io.IOException;
import java.util.Objects;

public class ParamsAddressDataTx extends ParamsAddressData {
    @JsonProperty("priv_key") private String privKey;
    @JsonProperty("fee") private long fee;
    @JsonProperty("gas_limit") private long gasLimit;

    public ParamsAddressDataTx(String address, String data, long fee, long gasLimit) {
        super(address, data);
        setPrivKey();
        this.fee = fee;
        this.gasLimit = gasLimit;
    }

    ParamsAddressDataTx(Types.TxType tx) {
        super(tx);
        setPrivKey();
        this.fee = HexValue.toBigInteger(tx.getGasPrice()).longValueExact();
        this.gasLimit = HexValue.toBigInteger(tx.getGas()).longValueExact();
    }

    private void setPrivKey() {
        try {
            this.privKey = Node.instance().getKey().getPrivate(); //TODO :: Replace with client-side cryptographic support for transaction signing
        } catch (IOException e) {
            logger.error("Failed to retrieve private key.", e);
        }
    }

    @Override public String toString() {
        return "{\"address\":\"" + address + "\",\"data\":\"" + data + "\",\"priv_key\":\"" + privKey + "\",\"fee\":" + fee + ",\"gas_limit\":" + gasLimit + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ParamsAddressDataTx that = (ParamsAddressDataTx) o;

        if (fee != that.fee) return false;
        if (gasLimit != that.gasLimit) return false;
        return Objects.equals(privKey, that.privKey);

    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (privKey != null ? privKey.hashCode() : 0);
        result = 31 * result + (int) (fee ^ (fee >>> 32));
        result = 31 * result + (int) (gasLimit ^ (gasLimit >>> 32));
        return result;
    }
}
