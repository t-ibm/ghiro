/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.util;

import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * The hex value encoding base helper class. See the <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC#hex-value-encoding">JSON-RPC Wiki</a> for more info.
 * Consequently, we use {@link BigInteger} to represent quantities and {@code byte[]} for raw data.
 */
public class HexValueBase {

    private static final String PREFIX = "0x";

    protected HexValueBase() {}

    private static boolean containsPrefix(String input) {
        return input.length() > 1 && input.charAt(0) == '0' && input.charAt(1) == 'x';
    }

    private static String validate(String value) {
        if (value == null) {
            throw new NumberFormatException("Value cannot be null.");
        } else if (!value.startsWith(PREFIX)) {
            value = addPrefix(value);
        }
        if (value.length() < 3) {
            throw new NumberFormatException("Value must be 0[xX][0-9a-fA-F]+.");
        }
        return value;
    }

    public static String getHash(String methodSignature) {
        return Hash.sha3(HexValueBase.encode(methodSignature));
    }

    public static String addPrefix(String input) {
        if (!containsPrefix(input)) {
            return PREFIX + input;
        } else {
            return input;
        }
    }

    public static String stripPrefix(String input) {
        if (containsPrefix(input)) {
            return input.substring(2);
        } else {
            return input;
        }
    }

    public static BigInteger toBigInteger(String value) {
        return new BigInteger(validate(value).substring(2), 16);
    }

    protected static BigInteger toBigInteger(byte[] value) {
        return new BigInteger(1, value);
    }

    protected static byte[] toByteArray(BigInteger value, int length) {
        byte[] result = new byte[length];
        byte[] bytes = value.toByteArray();

        int bytesLength;
        int srcOffset;
        if (bytes[0] == 0) {
            bytesLength = bytes.length - 1;
            srcOffset = 1;
        } else {
            bytesLength = bytes.length;
            srcOffset = 0;
        }

        if (bytesLength > length) {
            throw new RuntimeException("Input is too large to put in byte array of size " + length);
        }

        int destOffset = length - bytesLength;
        System.arraycopy(bytes, srcOffset, result, destOffset, bytesLength);
        return result;
    }

    public static byte[] toByteArray(String input) {
        String cleanInput = stripPrefix(input);

        int len = cleanInput.length();

        if (len == 0) {
            return new byte[]{};
        }

        byte[] data;
        int startIdx;
        if (len % 2 != 0) {
            data = new byte[(len / 2) + 1];
            data[0] = (byte) Character.digit(cleanInput.charAt(0), 16);
            startIdx = 1;
        } else {
            data = new byte[len / 2];
            startIdx = 0;
        }

        for (int i = startIdx; i < len; i += 2) {
            data[(i + 1) / 2] = (byte) ((Character.digit(cleanInput.charAt(i), 16) << 4)
                + Character.digit(cleanInput.charAt(i + 1), 16));
        }
        return data;
    }

    public static String toString(BigInteger value) {
        if (value.signum() != -1) {
            return PREFIX + value.toString(16);
        } else {
            throw new NumberFormatException("Negative values are not supported");
        }
    }

    public static String toString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return PREFIX + sb.toString();
    }

    public static String decode(byte[] bytes) {
        return  new String(bytes, Charset.defaultCharset()).trim();
    }

    public static String decode(String hexStr) {
        return new String(toByteArray(hexStr), Charset.defaultCharset()).trim();
    }

    public static String encode(String str) {
        return toString(str.getBytes(Charset.defaultCharset()));
    }
}