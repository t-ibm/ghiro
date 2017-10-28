/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.abi.util;

import com.softwareag.tom.abi.ContractInterface;
import com.softwareag.tom.util.Hash;
import com.softwareag.tom.util.HexValueBase;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a> encoding for constructors, functions, and events.
 */
public class SpecificationEncoder {

    private SpecificationEncoder() {}

    public static String encode(ContractInterface.Specification specification) {
        return null;
    }

    static int getParameterCount(List<ContractInterface.Parameter> parameters) {
        int count = 0;
        for (ContractInterface.Parameter parameter:parameters) {
                count += parameter.getLength();
        }
        return count;
    }

    static String getFunctionSignature(String methodName, List<ContractInterface.Parameter> parameters) {
        String params = parameters.stream().map(p -> String.valueOf(p.getType())).collect(Collectors.joining(","));
        return methodName + "(" + params + ")";
    }

    public static String getFunctionId(String methodSignature) {
        return HexValueBase.stripPrefix(Hash.sha3(HexValueBase.encode(methodSignature)).substring(0, 10));
    }
}