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
import java.util.List;

/**
 * ABI types implementation.
 */
final class ParameterTypeJava {

    static final UnknownType UNKNOWN = UnknownType.UNKNOWN;
    static final BoolType BOOL = BoolType.BOOL;
    static final NumericType INT = NumericType.INT;
    static final NumericType UINT = NumericType.UINT;
    static final NumericType ADDRESS = NumericType.ADDRESS;
    static final NumericType FIXED = NumericType.FIXED;
    static final NumericType UFIXED = NumericType.UFIXED;
    static final BytesType BYTES = BytesType.BYTES;
    static final StringType STRING = StringType.STRING;

    private enum UnknownType implements ParameterType<Object> {
        UNKNOWN;

        @Override public Class<Object> getType() { return Object.class; }
        @Override public Object asType(Object value) { return getType().cast(value); }
        @Override public String getName() { return getDeclaringClass().getName() + "." + name(); }
        @Override public boolean isDynamic() { return false; }
    }

    private enum BoolType implements ParameterType<Boolean> {
        BOOL("bool");

        private String name;

        BoolType(String name) {
            this.name = name;
        }

        @Override public Class<Boolean> getType() { return Boolean.class; }
        @Override public Boolean asType(Object value) { return getType().cast(value); }
        @Override public String getName() { return name; }
        @Override public boolean isDynamic() { return false; }
    }

    static class NumericType implements ParameterType<BigInteger> {
        static final NumericType INT = new NumericType("int256");
        static final NumericType UINT = new NumericType("uint256");
        static final NumericType ADDRESS = new NumericType("address");
        static final NumericType FIXED = new NumericType("fixed128x19");
        static final NumericType UFIXED = new NumericType("ufixed128x19");

        private String name;

        NumericType(String name) {
            this.name = name;
        }

        @Override public Class<BigInteger> getType() { return BigInteger.class; }
        @Override public BigInteger asType(Object value) { return value instanceof String ? getType().cast(HexValueBase.toBigInteger((String) value)) : getType().cast(value); }
        @Override public String getName() { return name; }
        @Override public boolean isDynamic() { return false; }
        boolean isUnsigned() { return name.charAt(0) == 'u' || name.equals("address"); }
        boolean isFixed() { return name.startsWith("fixed") || name.startsWith("ufixed"); }
    }

    static class BytesType implements ParameterType<byte[]> {
        static final BytesType BYTES = new BytesType("bytes", true);

        private String name;
        private boolean dynamic;

        BytesType(String name, boolean dynamic) {
            this.name = name;
            this.dynamic = dynamic;
        }

        @Override public Class<byte[]> getType() { return byte[].class; }
        @Override public byte[] asType(Object value) { return getType().cast(value); }
        @Override public String getName() { return name; }
        @Override public boolean isDynamic() { return dynamic; }
    }

    private enum StringType implements ParameterType<String> {
        STRING("string");

        private String name;

        StringType(String name) {
            this.name = name;
        }

        @Override public Class<String> getType() { return String.class; }
        @Override public String asType(Object value) { return getType().cast(value); }
        @Override public String getName() { return name; }
        @Override public boolean isDynamic() { return true; }
    }

    static class ArrayType implements ParameterType<List> {

        private String name;

        ArrayType(String name) {
            this.name = name;
        }

        @Override public Class<List> getType() { return List.class; }
        @Override public List asType(Object value) { return getType().cast(value); }
        @Override public String getName() { return name; }
        @Override public boolean isDynamic() {
            int start = name.trim().indexOf('[');
            int end = name.trim().indexOf(']');
            return end - start == 1;
        }
    }
}