/*
 * Copyright (c) 2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.util;

import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.contract.abi.ParameterType;
import com.softwareag.tom.util.HexValueBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decodes values returned by function or event calls.
 * Adapted from the <a href="https://github.com/web3j/web3j/blob/master/abi/src/main/java/org/web3j/abi/FunctionReturnDecoder.java">Web3 Java</a> implementation.
 */
public class ReturnDecoder extends ValueBase {

    private ReturnDecoder() {}

    /**
     * @param hexValue The contract's return value as a hex string
     * @param parameters The output parameters
     * @return the output parameter values as a list of Java types
     */
    public static <T> List<T> decode(List<? extends ContractInterface.Parameter<T>> parameters, String hexValue) {
        String input = HexValueBase.stripPrefix(hexValue);
        if (input.isEmpty()) {
            return Collections.emptyList();
        } else {
            return build(parameters, input);
        }
    }

    private static <T> List<T> build(List<? extends ContractInterface.Parameter<T>> parameters, String input) {
        List<T> results = new ArrayList<>(parameters.size());
        int offset = 0;
        for (ContractInterface.Parameter<T> parameter : parameters) {
            ParameterType<T> parameterType = parameter.getType();
            int hexStringDataOffset = getOffset(parameterType, input, offset);
            int length = parameterType.size();
            T result = parameter.decode(input, hexStringDataOffset);
            offset += length * MAX_BYTE_LENGTH_FOR_HEX_STRING;
            results.add(result);
        }
        return results;
    }

    private static <T> int getOffset(ParameterType<T> parameterType, String value, int offset) {
        if (parameterType.isDynamic()) {
            return ValueDecoder.decodeUintAsInt(value, offset) << 1;
        } else {
            return offset;
        }
    }
}