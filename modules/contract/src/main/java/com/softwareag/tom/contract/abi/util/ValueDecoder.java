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
public class ValueDecoder extends ValueBase {

    public static <T> T decode(ParameterType<T> type, String input) {
        return decode(type, input, 0);
    }

    @SuppressWarnings("unchecked") public static <T> T decode(ParameterType<T> type, String input, int offset) {
        if (type instanceof ParameterTypeJava.NumericType) {
            return (T) decodeNumeric((ParameterTypeJava.NumericType) type, input.substring(offset));
        } else if (type == ParameterTypeJava.BOOL) {
            return (T) decodeBool(input, offset);
        } else if (type instanceof ParameterTypeJava.BytesType) {
            return (T) decodeBytes((ParameterTypeJava.BytesType) type, input, offset);
        } else if (type == ParameterTypeJava.STRING) {
            return (T) decodeUtf8String(input, offset);
        } else if (type instanceof ParameterTypeJava.ArrayType) {
            return (T) decodeArray((ParameterTypeJava.ArrayType)type, input, offset);
        } else {
            throw new UnsupportedOperationException("Unknown type, value '" + input + "' cannot be decoded.");
        }
    }

    private static <T> List<T> decodeArray(ParameterTypeJava.ArrayType type, String input, int offset) {
        if (!type.isDynamic()) {
            return decodeStaticArray(type, input, offset);
        } else if (type.isDynamic()) {
            return decodeDynamicArray(type, input, offset);
        } else {
            throw new UnsupportedOperationException("Unsupported TypeReference: " + type.getName() + ", only Array types can be passed as TypeReferences");
        }
    }

    private static BigInteger decodeNumeric(ParameterTypeJava.NumericType type, String input) {
        try {
            byte[] inputByteArray = HexValueBase.toByteArray(input);
            int typeLengthAsBytes = getTypeLengthInBytes(type);

            byte[] resultByteArray = new byte[typeLengthAsBytes + 1];

            if (!type.isUnsigned()) {
                resultByteArray[0] = inputByteArray[0];  // take MSB as sign bit
            }

            int valueOffset = MAX_BYTE_LENGTH - typeLengthAsBytes;
            System.arraycopy(inputByteArray, valueOffset, resultByteArray, 1, typeLengthAsBytes);

            return new BigInteger(resultByteArray);
        } catch ( SecurityException | IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unable to create instance of " + type.getName(), e);
        }
    }

    private static int getTypeLengthInBytes(ParameterTypeJava.NumericType type) {
        int typeLength = getTypeLength(type);  // divide by 8
        return  typeLength >> 3;
    }

    private static int getTypeLength(ParameterTypeJava.NumericType type) {
        if (type == ParameterTypeJava.ADDRESS) {
            return ADDRESS_LENGTH;
        } else if (!type.isFixed()) {
            String regex = "(uint|int)";
            String[] splitName = type.getName().split(regex);
            if (splitName.length == 2) {
                return Integer.parseInt(splitName[1]);
            }
        } else if (type.isFixed()) {
            String regex = "(ufixed|fixed)";
            String[] splitName = type.getName().split(regex);
            if (splitName.length == 2) {
                String[] bitsCounts = splitName[1].split("x");
                return Integer.parseInt(bitsCounts[0]) + Integer.parseInt(bitsCounts[1]);
            }
        }
        return MAX_BIT_LENGTH;
    }

    public static int decodeUintAsInt(String rawInput, int offset) {
        String input = rawInput.substring(offset, offset + MAX_BYTE_LENGTH_FOR_HEX_STRING);
        return decode(ParameterTypeJava.UINT, input, 0).intValue();
    }

    private static Boolean decodeBool(String rawInput, int offset) {
        String input = rawInput.substring(offset, offset + MAX_BYTE_LENGTH_FOR_HEX_STRING);
        BigInteger numericValue = HexValueBase.toBigInteger(input);
        return numericValue.equals(BigInteger.ONE);
    }

    private static byte[] decodeBytes(ParameterTypeJava.BytesType type, String input, int offset) {
        if (type.isDynamic()) {
            return decodeDynamicBytes(input, offset);
        }
        try {
            String simpleName = type.getName();
            String[] splitName = simpleName.split("bytes");
            int length = Integer.parseInt(splitName[1]);
            int hexStringLength = length << 1;

            return HexValueBase.toByteArray(input.substring(offset, offset + hexStringLength));
        } catch (SecurityException | IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unable to create instance of " + type.getName(), e);
        }
    }

    private static byte[] decodeDynamicBytes(String input, int offset) {
        int encodedLength = decodeUintAsInt(input, offset);
        int hexStringEncodedLength = encodedLength << 1;

        int valueOffset = offset + MAX_BYTE_LENGTH_FOR_HEX_STRING;

        String data = input.substring(valueOffset,valueOffset + hexStringEncodedLength);
        return HexValueBase.toByteArray(data);
    }

    private static String decodeUtf8String(String input, int offset) {
        byte[] bytes = decodeDynamicBytes(input, offset);
        return HexValueBase.decode(bytes);
    }

    /**
     * Static array length cannot be passed as a type.
     */
    private static <T> List<T> decodeStaticArray(ParameterTypeJava.ArrayType type, String input, int offset) {

        BiFunction<List<T>, String, List<T>> function = (elements, typeName) -> {
            if (elements.isEmpty()) {
                throw new UnsupportedOperationException("Zero length fixed array is invalid type");
            } else {
                return (List<T>) new ArrayList<>(elements);
            }
        };

        ParameterType<T> baseType =  getItemType(type);
        int length = type.size();

        return decodeArrayElements(input, offset, baseType, length, function);
    }

    private static <T> List<T> decodeDynamicArray(ParameterTypeJava.ArrayType type, String input, int offset) {

        int length = decodeUintAsInt(input, offset);

        BiFunction<List<T>, String, List<T>> function = (elements, typeName) -> {
            if (elements.isEmpty()) {
                return (List<T>) new ArrayList<T>();
            } else {
                return (List<T>) new ArrayList<>(elements);
            }
        };

        int valueOffset = offset + MAX_BYTE_LENGTH_FOR_HEX_STRING;

        ParameterType<T> baseType =  getItemType(type);

        return decodeArrayElements(input, valueOffset, baseType, length, function);
    }

    private static <T> ParameterType<T> getItemType(ParameterTypeJava.ArrayType type) {
        String baseType = type.getName().substring(0, type.getName().indexOf('['));
        return ValueEncoder.parse(baseType);
    }

    private static <T> List<T> decodeArrayElements(String input, int offset, ParameterType<T> type, int length, BiFunction<List<T>, String, List<T>> consumer) {
        String name = type.getName();
        if (name.trim().contains("[")) {
            throw new UnsupportedOperationException("Arrays of arrays are not currently supported for external functions, see http://solidity.readthedocs.io/en/develop/types.html#members");
        } else {
            List<T> elements = new ArrayList<>(length);

            for (int i = 0, currOffset = offset;
                    i < length;
                    i++, currOffset += getSingleElementLength(type, input, currOffset)
                         * MAX_BYTE_LENGTH_FOR_HEX_STRING) {
                T value = decode(type, input, currOffset);
                elements.add(value);
            }
            return consumer.apply(elements, type.getName());
        }
    }

    private static int getSingleElementLength(ParameterType type, String input, int offset) {
        if (input.length() == offset) {
            return 0;
        } else if (type == ParameterTypeJava.BYTES || type ==  ParameterTypeJava.STRING) {
            // length field + data value
            return (decodeUintAsInt(input, offset) / MAX_BYTE_LENGTH) + 2;
        } else {
            return 1;
        }
    }
}
