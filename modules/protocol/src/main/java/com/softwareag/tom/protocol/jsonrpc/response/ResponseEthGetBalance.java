/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;
import com.softwareag.tom.protocol.util.HexValue;

import java.util.Objects;

/**
 * {@code eth_getBalance}.
 */
public class ResponseEthGetBalance extends Response<ResponseEthGetBalance.Result, Types.ResponseEthGetBalance> {

    public ResponseEthGetBalance() {
        super();
    }

    public ResponseEthGetBalance(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthGetBalance(String address, long balance) {
        super();
        this.result = new Result(address, balance);
    }

    public Types.ResponseEthGetBalance getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthGetBalance.newBuilder().setBalance(HexValue.toByteString(this.result.balance)).build();
        }
    }

    final static class Result {
        @JsonProperty("address") String address;
        @JsonProperty("balance") long balance;
        @JsonProperty("code") String code;
        @JsonProperty("pub_key") String pubKey;
        @JsonProperty("sequence") long sequence;
        @JsonProperty("storage_root") String storageRoot;

        private Result() {}

        private Result(String address, long balance) {
            this();
            this.address = address;
            this.balance = balance;
            this.code = "";
            this.storageRoot = "";
        }

        @Override public String toString() {
            return "{\"address\":\"" + address + "\", \"balance\":" + balance + ", \"code\":\"" + code + "\", \"pub_key\":" + pubKey + ", \"sequence\":" + sequence + ", \"storage_root\":\"" + storageRoot + "\"}";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            if (balance != result.balance) return false;
            if (sequence != result.sequence) return false;
            if (!address.equals(result.address)) return false;
            if (!code.equals(result.code)) return false;
            if (!Objects.equals(pubKey, result.pubKey)) return false;
            return storageRoot.equals(result.storageRoot);
        }

        @Override public int hashCode() {
            int result = address.hashCode();
            result = 31 * result + (int) (balance ^ (balance >>> 32));
            result = 31 * result + code.hashCode();
            result = 31 * result + (pubKey != null ? pubKey.hashCode() : 0);
            result = 31 * result + (int) (sequence ^ (sequence >>> 32));
            result = 31 * result + storageRoot.hashCode();
            return result;
        }
    }
}