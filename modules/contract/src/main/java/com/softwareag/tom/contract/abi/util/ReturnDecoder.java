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
     * @param hexValue         The contract's return valu as a hex string
     * @param outputParameters The output parameters
     * @return the output parameter values as a list of Java types
     */
    public static <T> List<T> decode(List<? extends ContractInterface.Parameter<T>> outputParameters, String hexValue) {
        String input = HexValueBase.stripPrefix(hexValue);

        if (input.isEmpty()) {
            return Collections.emptyList();
        } else {
            return build(outputParameters, input);
        }
    }

    private static <T> List<T> build(List<? extends ContractInterface.Parameter<T>> outputParameters, String input) {
        List<T> results = new ArrayList<>(outputParameters.size());

        int offset = 0;
        for (ContractInterface.Parameter<T> parameterType : outputParameters) {
            ParameterType<T> type = parameterType.getType();
            int hexStringDataOffset = getOffset(type, input, offset);
            int length = type.size();
            T result = parameterType.decode(input, hexStringDataOffset);
            offset += length * MAX_BYTE_LENGTH_FOR_HEX_STRING;
            results.add(result);
        }
        return results;
    }

    private static <T> int getOffset(ParameterType<T> type, String value, int offset) {
        if (type == ParameterTypeJava.BYTES || type == ParameterTypeJava.STRING || (type instanceof ParameterTypeJava.ArrayType && ((ParameterTypeJava.ArrayType) type).isDynamic())) {
            return ValueDecoder.decodeUintAsInt(value, offset) << 1;
        } else {
            return offset;
        }
    }
}