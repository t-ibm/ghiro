/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.util;

import com.softwareag.tom.contract.abi.ParameterType;
import com.softwareag.tom.util.HexValueBase;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#types">Ethereum Contract ABI</a> encoding for values.
 * <p>
 * Adapted from the <a href="https://github.com/web3j/web3j/blob/master/abi/src/main/java/org/web3j/abi/TypeEncoder.java">Web3 Java</a> implementation.
 */
public class ValueEncoder {

    private final static int MAX_BIT_LENGTH = 256;
    final static int MAX_BYTE_LENGTH = MAX_BIT_LENGTH / 8;

    private ValueEncoder() {}

    public static ParameterType parse(String type) {
        if (getDefaultType(type) != ParameterTypeJava.UNKNOWN) {
            return getDefaultType(type);
        } else if (isArray(type)) {
            return new ParameterTypeJava.ArrayType(type);
        } else if (type.startsWith("uint") || type.startsWith("ufixed") || type.startsWith("int") || type.startsWith("fixed")) {
            return new ParameterTypeJava.NumericType(type);
        } else if (type.startsWith("bytes")) {
            return new ParameterTypeJava.BytesType(type, false);
        } else {
            return ParameterTypeJava.UNKNOWN;
        }
    }

    private static ParameterType getDefaultType(String type) {
        if ("address".equals(type)) {
            return ParameterTypeJava.ADDRESS;
        } else if ("uint".equals(type) || "uint256".equals(type)) {
            return ParameterTypeJava.UINT;
        } else if ("int".equals(type) || "int256".equals(type)) {
            return ParameterTypeJava.INT;
        } else if ("ufixed".equals(type) || "ufixed128x19".equals(type)) {
            return ParameterTypeJava.UFIXED;
        } else if ("fixed".equals(type) || "fixed128x19".equals(type)) {
            return ParameterTypeJava.FIXED;
        } else if ("bool".equals(type)) {
            return ParameterTypeJava.BOOL;
        } else if ("string".equals(type)) {
            return ParameterTypeJava.STRING;
        } else if ("bytes".equals(type)) {
            return ParameterTypeJava.BYTES;
        } else {
            return ParameterTypeJava.UNKNOWN;
        }
    }
    private static boolean isArray(String type) {
        int start = type.trim().indexOf('[');
        int end = type.trim().indexOf(']');
        return end - start > 0;
    }

    public static <T> String encode(ParameterType type, T value) {
        if (type instanceof ParameterTypeJava.NumericType && value instanceof BigInteger) {
            return encodeNumeric(type, (BigInteger) value);
        } else if (type == ParameterTypeJava.BOOL && value instanceof Boolean) {
            return encodeBool((Boolean) value);
        } else if (type instanceof ParameterTypeJava.BytesType && value instanceof byte[]) {
            return encodeBytes(type, (byte[]) value);
        } else if (type == ParameterTypeJava.STRING && value instanceof String) {
            return encodeString((String) value);
        } else if (type instanceof ParameterTypeJava.ArrayType && value instanceof List) {
            String baseType = type.getName().substring(0, type.getName().indexOf('['));
            return encodeArray(parse(baseType), ((ParameterTypeJava.ArrayType) type).isDynamic(), (List<?>) value);
        } else {
            throw new UnsupportedOperationException("Value of type '" + value.getClass() + "' cannot be encoded as '" + type.getName() + "'.");
        }
    }

    static String encodeNumeric(ParameterType type, BigInteger value) {
        byte[] rawValue = toByteArray(type, value);
        byte paddingValue = getPaddingValue(value);
        byte[] paddedRawValue = new byte[MAX_BYTE_LENGTH];
        if (paddingValue != 0) {
            for (int i = 0; i < paddedRawValue.length; i++) {
                paddedRawValue[i] = paddingValue;
            }
        }

        System.arraycopy(
            rawValue, 0,
            paddedRawValue, MAX_BYTE_LENGTH - rawValue.length,
            rawValue.length);
        return HexValueBase.stripPrefix(HexValueBase.toString(paddedRawValue));
    }

    private static byte getPaddingValue(BigInteger value) {
        if (value.signum() == -1) {
            return (byte) 0xff;
        } else {
            return 0;
        }
    }

    private static byte[] toByteArray(ParameterType type, BigInteger value) {
        if (((ParameterTypeJava.NumericType) type).isUnsigned() && value.bitLength() == MAX_BIT_LENGTH) {
            // As BigInteger is signed, if we have a 256 bit value, the resultant byte array
            // will contain a sign byte in it's MSB, which we should ignore for this unsigned
            // integer type.
            byte[] byteArray = new byte[MAX_BYTE_LENGTH];
            System.arraycopy(value.toByteArray(), 1, byteArray, 0, MAX_BYTE_LENGTH);
            return byteArray;
        }
        return value.toByteArray();
    }

    private static String encodeBool(Boolean value) {
        byte[] rawValue = new byte[MAX_BYTE_LENGTH];
        if (value) {
            rawValue[rawValue.length - 1] = 1;
        }
        return HexValueBase.stripPrefix(HexValueBase.toString(rawValue));
    }

    private static String encodeString(String value) {
        byte[] utfEncoded = value.getBytes(StandardCharsets.UTF_8);
        return encodeBytes(ParameterTypeJava.BYTES, utfEncoded);
    }

    private static String encodeBytes(ParameterType type, byte[] value) {
        if (!((ParameterTypeJava.BytesType) type).isDynamic()) {
            return encodeBytes(value);
        }
        int size = value.length;
        String encodedLength = encodeNumeric(ParameterTypeJava.UINT, BigInteger.valueOf(size));
        String encodedValue = encodeBytes(value);

        return encodedLength + encodedValue;
    }

    private static String encodeBytes(byte[] value) {
        int length = value.length;
        int mod = length % MAX_BYTE_LENGTH;

        byte[] dest;
        if (mod != 0) {
            int padding = MAX_BYTE_LENGTH - mod;
            dest = new byte[length + padding];
            System.arraycopy(value, 0, dest, 0, length);
        } else {
            dest = value;
        }
        return HexValueBase.stripPrefix(HexValueBase.toString(dest));
    }

    private static <T> String encodeArray(ParameterType type, boolean isDynamic, List<T> values) {
        if (!isDynamic) {
            return encodeArray(type, values);
        }
        int size = values.size();
        String encodedLength = encodeNumeric(ParameterTypeJava.UINT, BigInteger.valueOf(size));
        String encodedValues = encodeArray(type, values);

        return encodedLength + encodedValues;
    }

    private static <T> String encodeArray(ParameterType type, List<T> values) {
        StringBuilder result = new StringBuilder();
        for (T value : values) {
            result.append(ValueEncoder.encode(type, value));
        }
        return result.toString();
    }
}
