/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract;

import com.softwareag.tom.contract.abi.ContractInterface;

import java.math.BigInteger;

/**
 * Smart contract abstraction.
 */
public class Contract {
    //https://blog.ethereum.org/2016/10/31/uncle-rate-transaction-fee-analysis/
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    private final ContractInterface abi;
    private final String binary;
    private String contractAddress;
    private final BigInteger gasPrice;
    private final BigInteger gasLimit;

    private Contract(ContractInterface abi, String binary, BigInteger gasPrice, BigInteger gasLimit) {
        this.abi = abi;
        this.binary = binary;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
    }

    static Contract create(ContractInterface contractAbi, String contractBinary) {
        return new Contract(contractAbi, contractBinary, GAS_PRICE, GAS_LIMIT);
    }

    public ContractInterface getAbi() {
        return abi;
    }
    public String getBinary() {
        return binary;
    }
    public BigInteger getGasPrice() { return gasPrice; }
    public BigInteger getGasLimit() { return gasLimit; }
}
