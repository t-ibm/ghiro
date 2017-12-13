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
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    private final ContractInterface contractAbi;
    private final String contractBinary;
    private String contractAddress;
    private final BigInteger gasPrice;
    private final BigInteger gasLimit;

    private Contract(ContractInterface contractAbi, String contractBinary, BigInteger gasPrice, BigInteger gasLimit) {
        this.contractAbi = contractAbi;
        this.contractBinary = contractBinary;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
    }

    public static Contract create(ContractInterface contractAbi, String contractBinary) {
        return new Contract(contractAbi, contractBinary, GAS_PRICE, GAS_LIMIT);
    }

    public ContractInterface getContractAbi() {
        return contractAbi;
    }

    public String getContractBinary() {
        return contractBinary;
    }
}
