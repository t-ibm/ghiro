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
public final class ValueEncoder extends ValueBase {

    private ValueEncoder() {}

    /**
     * @param parameterType The parameter type
     * @param value The parameter value as a Java type
     * @param <T> The Java type
     * @return the parameter value as hex string
     */
    public static <T> String encode(ParameterType parameterType, T value) {
        if (parameterType instanceof ParameterTypeJava.NumericType && value instanceof BigInteger) {
            return encodeNumeric((ParameterTypeJava.NumericType)parameterType, (BigInteger) value);
        } else if (parameterType == ParameterTypeJava.BOOL && value instanceof Boolean) {
            return encodeBool((Boolean) value);
        } else if (parameterType instanceof ParameterTypeJava.BytesType && value instanceof byte[]) {
            return encodeBytes(parameterType, (byte[]) value);
        } else if (parameterType == ParameterTypeJava.STRING && value instanceof String) {
            return encodeString((String) value);
        } else if (parameterType instanceof ParameterTypeJava.ArrayType && value instanceof List) {
            String baseType = parameterType.getName().substring(0, parameterType.getName().indexOf('['));
            return encodeArray(parse(baseType), parameterType.isDynamic(), (List<?>) value);
        } else {
            throw new UnsupportedOperationException("Value of type '" + value.getClass() + "' cannot be encoded as '" + parameterType.getName() + "'.");
        }
    }

    /**
     * @param numericType The parameter type
     * @param value The parameter value as a {@link BigInteger}
     * @return the parameter value as hex string
     */
    static String encodeNumeric(ParameterTypeJava.NumericType numericType, BigInteger value) {
        byte[] rawValue = toByteArray(numericType, value);
        byte paddingValue = getPaddingValue(value);
        byte[] paddedRawValue = new byte[MAX_BYTE_LENGTH];
        if (paddingValue != 0) {
            for (int i = 0; i < paddedRawValue.length; i++) {
                paddedRawValue[i] = paddingValue;
            }
        }
        System.arraycopy(rawValue, 0, paddedRawValue, MAX_BYTE_LENGTH - rawValue.length, rawValue.length);
        return HexValueBase.stripPrefix(HexValueBase.toString(paddedRawValue));
    }

    private static byte getPaddingValue(BigInteger value) {
        if (value.signum() == -1) {
            return (byte) 0xff;
        } else {
            return 0;
        }
    }

    private static byte[] toByteArray(ParameterTypeJava.NumericType numericType, BigInteger value) {
        if (numericType.isUnsigned() && value.bitLength() == MAX_BIT_LENGTH) {
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

    private static String encodeBytes(ParameterType parameterType, byte[] value) {
        if (!parameterType.isDynamic()) {
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

    private static <T> String encodeArray(ParameterType<T> parameterType, boolean isDynamic, List<T> values) {
        if (!isDynamic) {
            return encodeArray(parameterType, values);
        }
        int size = values.size();
        String encodedLength = encodeNumeric(ParameterTypeJava.UINT, BigInteger.valueOf(size));
        String encodedValues = encodeArray(parameterType, values);

        return encodedLength + encodedValues;
    }

    private static <T> String encodeArray(ParameterType<T> parameterType, List<T> values) {
        StringBuilder result = new StringBuilder();
        for (T value : values) {
            result.append(ValueEncoder.encode(parameterType, value));
        }
        return result.toString();
    }
}
