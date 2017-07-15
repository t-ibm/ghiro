/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthSendTransaction;

/**
 * {@code eth_sendTransaction}.
 */
public class RequestEthSendTransaction extends Request<RequestEthSendTransaction.Params, ResponseEthSendTransaction> {
    public RequestEthSendTransaction(Service jsonRpcService, Types.RequestEthSendTransaction msg) {
        super(jsonRpcService, "burrow.transactAndHold", new Params(msg.getTx()));
    }

    static class Params {
        @JsonProperty("priv_key") public String privKey;
        @JsonProperty("address") public String address;
        @JsonProperty("data") public String data;
        @JsonProperty("fee") public long fee;
        @JsonProperty("gas_limit") public long gasLimit;

        Params(Types.TxType tx) {
            this.privKey = "6B72D45EB65F619F11CE580C8CAED9E0BADC774E9C9C334687A65DCBAD2C4151CB3688B7561D488A2A4834E1AEE9398BEF94844D8BDBBCA980C11E3654A45906"; //TODO
            this.address = validate(tx.getTo());
            this.data = tx.getData().toStringUtf8();
            this.fee = tx.getGas();
            this.gasLimit = tx.getGasPrice();
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Params params = (Params) o;

            if (fee != params.fee) return false;
            if (gasLimit != params.gasLimit) return false;
            if (privKey != null ? !privKey.equals(params.privKey) : params.privKey != null) return false;
            if (address != null ? !address.equals(params.address) : params.address != null) return false;
            return data != null ? data.equals(params.data) : params.data == null;

        }

        @Override public int hashCode() {
            int result = privKey != null ? privKey.hashCode() : 0;
            result = 31 * result + (address != null ? address.hashCode() : 0);
            result = 31 * result + (data != null ? data.hashCode() : 0);
            result = 31 * result + (int) (fee ^ (fee >>> 32));
            result = 31 * result + (int) (gasLimit ^ (gasLimit >>> 32));
            return result;
        }
    }
}