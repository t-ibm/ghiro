/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;
import com.softwareag.tom.protocol.util.HexValue;
import com.softwareag.tom.util.HexValueBase;

import java.math.BigInteger;

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

    public ResponseEthGetBalance(String balance) {
        super();
        this.result = new Result(balance);
    }

    public Types.ResponseEthGetBalance getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            return Types.ResponseEthGetBalance.newBuilder().setBalance(HexValue.toByteString(this.result.balance)).build();
        }
    }

    final static class Result {
        BigInteger balance;

        private Result() {}

        private Result(String balance) {
            this();
            this.balance = HexValueBase.toBigInteger(balance);
        }

        @Override public String toString() {
            return '"' + HexValueBase.toString(balance) + '"';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;

            Result result = (Result) o;

            return balance.equals(result.balance);
        }

        @Override public int hashCode() {
            return balance.hashCode();
        }
    }
}