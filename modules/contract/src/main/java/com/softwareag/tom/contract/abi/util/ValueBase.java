/*
 * Copyright (c) 2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.util;

import com.softwareag.tom.contract.abi.ParameterType;

/**
 * The contract value encoding/decoding base helper class.
 */
public abstract class ValueBase {

    static final int ADDRESS_LENGTH = 160;
    static final int MAX_BIT_LENGTH = 256;
    static final int MAX_BYTE_LENGTH = MAX_BIT_LENGTH / 8;
    static final int MAX_BYTE_LENGTH_FOR_HEX_STRING = MAX_BYTE_LENGTH << 1;

    /**
     * @param type The parameter type name
     * @return the parameter type as {@link ParameterType}
     */
    @SuppressWarnings("unchecked") public static <T> ParameterType<T> parse(String type) {
        ParameterType parameterType;
        if (getDefaultType(type) != ParameterTypeJava.UNKNOWN) {
            parameterType = getDefaultType(type);
        } else if (isArray(type)) {
            parameterType = new ParameterTypeJava.ArrayType(type);
        } else if (type.startsWith("uint") || type.startsWith("ufixed") || type.startsWith("int") || type.startsWith("fixed")) {
            parameterType = new ParameterTypeJava.NumericType(type);
        } else if (type.startsWith("bytes")) {
            parameterType = new ParameterTypeJava.BytesType(type, false);
        } else {
            parameterType = ParameterTypeJava.UNKNOWN;
        }
        return parameterType;
    }

    @SuppressWarnings("unchecked") private static <T> ParameterType<T> getDefaultType(String type) {
        ParameterType parameterType;
        if ("address".equals(type)) {
            parameterType =  ParameterTypeJava.ADDRESS;
        } else if ("uint".equals(type) || "uint256".equals(type)) {
            parameterType =  ParameterTypeJava.UINT;
        } else if ("int".equals(type) || "int256".equals(type)) {
            parameterType =  ParameterTypeJava.INT;
        } else if ("ufixed".equals(type) || "ufixed128x19".equals(type)) {
            parameterType =  ParameterTypeJava.UFIXED;
        } else if ("fixed".equals(type) || "fixed128x19".equals(type)) {
            parameterType =  ParameterTypeJava.FIXED;
        } else if ("bool".equals(type)) {
            parameterType =  ParameterTypeJava.BOOL;
        } else if ("string".equals(type)) {
            parameterType =  ParameterTypeJava.STRING;
        } else if ("bytes".equals(type)) {
            parameterType =  ParameterTypeJava.BYTES;
        } else {
            parameterType =  ParameterTypeJava.UNKNOWN;
        }
        return parameterType;
    }

    private static boolean isArray(String type) {
        int start = type.trim().indexOf('[');
        int end = type.trim().indexOf(']');
        return end - start > 0;
    }
}
