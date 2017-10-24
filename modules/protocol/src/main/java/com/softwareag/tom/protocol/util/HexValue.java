/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.util;

import com.google.protobuf.ByteString;
import com.softwareag.tom.util.HexValueBase;

import java.math.BigInteger;

/**
 * The hex value encoding helper class with added support of {@link ByteString}.
 */
public final class HexValue extends HexValueBase {

    private HexValue() {}

    public static String stripPrefix(ByteString input) {
        return stripPrefix(toString(input));
    }

    public static byte[] toByteArray(ByteString value) { return toByteArray(value.toStringUtf8()); }

    public static String toString(ByteString value) {
        return value.toStringUtf8();
    }

    public static BigInteger toBigInteger(ByteString value) {
        return toBigInteger(value.toStringUtf8());
    }

    public static ByteString toByteString(byte[] bytes) {
        return ByteString.copyFromUtf8(toString(bytes));
    }

    public static ByteString toByteString(String value) { return ByteString.copyFromUtf8(addPrefix(value)); }

    public static ByteString toByteString(BigInteger value) {
        return ByteString.copyFromUtf8(toString(value));
    }

    public static ByteString toByteString(long value) { return ByteString.copyFromUtf8(toString(BigInteger.valueOf(value))); }
}