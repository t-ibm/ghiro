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
     * @param typeName The parameter type name
     * @return the parameter type as {@link ParameterType}
     */
    @SuppressWarnings("unchecked") public static <T> ParameterType<T> parse(String typeName) {
        ParameterType parameterType;
        if (getDefaultType(typeName) != ParameterTypeJava.UNKNOWN) {
            parameterType = getDefaultType(typeName);
        } else if (isArray(typeName)) {
            parameterType = new ParameterTypeJava.ArrayType(typeName);
        } else if (typeName.startsWith("uint") || typeName.startsWith("ufixed") || typeName.startsWith("int") || typeName.startsWith("fixed")) {
            parameterType = new ParameterTypeJava.NumericType(typeName);
        } else if (typeName.startsWith("bytes")) { // Fixed-size bytes
            parameterType = new ParameterTypeJava.BytesType(typeName, false);
        } else {
            parameterType = ParameterTypeJava.UNKNOWN;
        }
        return parameterType;
    }

    @SuppressWarnings("unchecked") private static <T> ParameterType<T> getDefaultType(String typeName) {
        ParameterType parameterType;
        if ("address".equals(typeName)) {
            parameterType =  ParameterTypeJava.ADDRESS;
        } else if ("uint".equals(typeName) || "uint256".equals(typeName)) {
            parameterType =  ParameterTypeJava.UINT;
        } else if ("int".equals(typeName) || "int256".equals(typeName)) {
            parameterType =  ParameterTypeJava.INT;
        } else if ("ufixed".equals(typeName) || "ufixed128x19".equals(typeName)) {
            parameterType =  ParameterTypeJava.UFIXED;
        } else if ("fixed".equals(typeName) || "fixed128x19".equals(typeName)) {
            parameterType =  ParameterTypeJava.FIXED;
        } else if ("bool".equals(typeName)) {
            parameterType =  ParameterTypeJava.BOOL;
        } else if ("string".equals(typeName)) {
            parameterType =  ParameterTypeJava.STRING;
        } else if ("bytes".equals(typeName)) { // Dynamic bytes
            parameterType =  ParameterTypeJava.BYTES;
        } else {
            parameterType =  ParameterTypeJava.UNKNOWN;
        }
        return parameterType;
    }

    private static boolean isArray(String typeName) {
        int start = typeName.trim().indexOf('[');
        int end = typeName.trim().indexOf(']');
        return end - start > 0;
    }
}
