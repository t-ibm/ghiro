/*
 * Copyright (c) 2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.util;

import com.softwareag.tom.contract.abi.ParameterType;
import com.softwareag.tom.util.HexValueBase;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * The <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#types">Ethereum Contract ABI</a> decoding for values.
 * <p>
 * Adapted from the <a href="https://github.com/web3j/web3j/blob/master/abi/src/main/java/org/web3j/abi/TypeDecoder.java">Web3 Java</a> implementation.
 */
public final class ValueDecoder extends ValueBase {

    private ValueDecoder() {}

    /**
     * @param parameterType The parameter type
     * @param hexValue The parameter value as hex string
     * @param <T> The Java type
     * @return the parameter value as a Java type
     */
    public static <T> T decode(ParameterType<T> parameterType, String hexValue) {
        return decode(parameterType, hexValue, 0);
    }

    /**
     * @param parameterType The parameter type
     * @param hexValue The parameter value as hex string
     * @param offset The index of the hex string where this particular parameter value starts
     * @param <T> The Java type
     * @return the parameter value as a Java type
     */
    @SuppressWarnings("unchecked") public static <T> T decode(ParameterType<T> parameterType, String hexValue, int offset) {
        if (parameterType instanceof ParameterTypeJava.NumericType) {
            return (T) decodeNumeric((ParameterTypeJava.NumericType) parameterType, hexValue.substring(offset));
        } else if (parameterType == ParameterTypeJava.BOOL) {
            return (T) decodeBool(hexValue, offset);
        } else if (parameterType instanceof ParameterTypeJava.BytesType) {
            return (T) decodeBytes(parameterType, hexValue, offset);
        } else if (parameterType == ParameterTypeJava.STRING) {
            return (T) decodeString(hexValue, offset);
        } else if (parameterType instanceof ParameterTypeJava.ArrayType) {
            return (T) decodeArray(parameterType, hexValue, offset);
        } else {
            throw new UnsupportedOperationException("Unknown type, value '" + hexValue + "' cannot be decoded.");
        }
    }

    /**
     * @param hexValue The parameter value as hex string
     * @param offset The index of the hex string where this particular parameter value starts
     * @return the value as an {@code int}
     */
    static int decodeUintAsInt(String hexValue, int offset) {
        String input = hexValue.substring(offset, offset + MAX_BYTE_LENGTH_FOR_HEX_STRING);
        return decode(ParameterTypeJava.UINT, input, 0).intValue();
    }

    private static <T> List<T> decodeArray(ParameterType<T> parameterType, String input, int offset) {
        if (!parameterType.isDynamic()) {
            return decodeStaticArray(parameterType, input, offset);
        } else if (parameterType.isDynamic()) {
            return decodeDynamicArray(parameterType, input, offset);
        } else {
            throw new UnsupportedOperationException("Unsupported TypeReference: " + parameterType.getName() + ", only Array types can be passed as TypeReferences");
        }
    }

    private static BigInteger decodeNumeric(ParameterTypeJava.NumericType numericType, String input) {
        byte[] inputByteArray = HexValueBase.toByteArray(input);
        int typeLengthAsBytes = getTypeLengthInBytes(numericType);

        byte[] resultByteArray = new byte[typeLengthAsBytes + 1];

        if (!numericType.isUnsigned()) {
            resultByteArray[0] = inputByteArray[0];  // take MSB as sign bit
        }

        int valueOffset = MAX_BYTE_LENGTH - typeLengthAsBytes;
        System.arraycopy(inputByteArray, valueOffset, resultByteArray, 1, typeLengthAsBytes);

        return new BigInteger(resultByteArray);
    }

    private static int getTypeLengthInBytes(ParameterTypeJava.NumericType numericType) {
        int typeLength = getTypeLength(numericType);  // divide by 8
        return  typeLength >> 3;
    }

    private static int getTypeLength(ParameterTypeJava.NumericType numericType) {
        if (numericType == ParameterTypeJava.ADDRESS) {
            return ADDRESS_LENGTH;
        } else if (!numericType.isFixed()) {
            String regex = "(uint|int)";
            String[] splitName = numericType.getName().split(regex);
            if (splitName.length == 2) {
                return Integer.parseInt(splitName[1]);
            }
        } else if (numericType.isFixed()) {
            String regex = "(ufixed|fixed)";
            String[] splitName = numericType.getName().split(regex);
            if (splitName.length == 2) {
                String[] bitsCounts = splitName[1].split("x");
                return Integer.parseInt(bitsCounts[0]) + Integer.parseInt(bitsCounts[1]);
            }
        }
        return MAX_BIT_LENGTH;
    }

    private static Boolean decodeBool(String rawInput, int offset) {
        String input = rawInput.substring(offset, offset + MAX_BYTE_LENGTH_FOR_HEX_STRING);
        BigInteger numericValue = HexValueBase.toBigInteger(input);
        return numericValue.equals(BigInteger.ONE);
    }

    private static <T> byte[] decodeBytes(ParameterType<T> parameterType, String input, int offset) {
        if (parameterType.isDynamic()) {
            return decodeBytes(input, offset);
        }
        String simpleName = parameterType.getName();
        String[] splitName = simpleName.split("bytes");
        int length = Integer.parseInt(splitName[1]);
        int hexStringLength = length << 1;
        return HexValueBase.toByteArray(input.substring(offset, offset + hexStringLength));
    }

    private static byte[] decodeBytes(String input, int offset) {
        int encodedLength = decodeUintAsInt(input, offset);
        int hexStringEncodedLength = encodedLength << 1;

        int valueOffset = offset + MAX_BYTE_LENGTH_FOR_HEX_STRING;

        String data = input.substring(valueOffset,valueOffset + hexStringEncodedLength);
        return HexValueBase.toByteArray(data);
    }

    private static String decodeString(String input, int offset) {
        byte[] bytes = decodeBytes(input, offset);
        return HexValueBase.decode(bytes);
    }

    /**
     * Static array length cannot be passed as a type.
     */
    private static <T> List<T> decodeStaticArray(ParameterType<T> parameterType, String input, int offset) {

        BiFunction<List<T>, String, List<T>> function = (elements, typeName) -> {
            if (elements.isEmpty()) {
                throw new UnsupportedOperationException("Zero length fixed array is invalid type.");
            } else {
                return (List<T>) new ArrayList<>(elements);
            }
        };

        ParameterType<T> baseType =  getItemType(parameterType);
        int length = parameterType.size();

        return decodeArrayElements(input, offset, baseType, length, function);
    }

    private static <T> List<T> decodeDynamicArray(ParameterType<T> parameterType, String input, int offset) {

        int length = decodeUintAsInt(input, offset);

        BiFunction<List<T>, String, List<T>> function = (elements, typeName) -> {
            if (elements.isEmpty()) {
                return (List<T>) new ArrayList<T>();
            } else {
                return (List<T>) new ArrayList<>(elements);
            }
        };

        int valueOffset = offset + MAX_BYTE_LENGTH_FOR_HEX_STRING;

        ParameterType<T> baseType =  getItemType(parameterType);

        return decodeArrayElements(input, valueOffset, baseType, length, function);
    }

    private static <T> ParameterType<T> getItemType(ParameterType<T> parameterType) {
        String baseType = parameterType.getName().substring(0, parameterType.getName().lastIndexOf('['));
        return ValueEncoder.parse(baseType);
    }

    private static <T> List<T> decodeArrayElements(String input, int offset, ParameterType<T> parameterType, int length, BiFunction<List<T>, String, List<T>> consumer) {
        String name = parameterType.getName();
        if (name.trim().contains("[")) {
            throw new UnsupportedOperationException("Arrays of arrays are not currently supported for external functions, see http://solidity.readthedocs.io/en/develop/types.html#members.");
        } else {
            List<T> elements = new ArrayList<>(length);

            for (int i = 0, currOffset = offset;
                    i < length;
                    i++, currOffset += getSingleElementLength(parameterType, input, currOffset)
                         * MAX_BYTE_LENGTH_FOR_HEX_STRING) {
                T value = decode(parameterType, input, currOffset);
                elements.add(value);
            }
            return consumer.apply(elements, parameterType.getName());
        }
    }

    private static int getSingleElementLength(ParameterType parameterType, String input, int offset) {
        if (input.length() == offset) {
            return 0;
        } else if (parameterType == ParameterTypeJava.BYTES || parameterType ==  ParameterTypeJava.STRING) {
            // length field + data value
            return (decodeUintAsInt(input, offset) / MAX_BYTE_LENGTH) + 2;
        } else {
            return 1;
        }
    }
}