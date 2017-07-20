/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.util

import spock.lang.Specification

/**
 * System under specification: {@link HexValue}.
 * @author tglaeser
 */
class HexValueTest extends Specification {

    def 'test strip prefix'() {
        expect:
        HexValue.stripPrefix(source) == target

        where:
        source << ['0x0', '0x001', '0x41', '0x0400', '0x400', '0x7fffffffffffffff']
        target << ['0', '001', '41', '0400', '400', '7fffffffffffffff']
    }

    def 'test add prefix'() {
        expect:
        HexValue.addPrefix(source) == target

        where:
        source << ['0', '0x001', '0x41', '0400', '400', '7fffffffffffffff']
        target << ['0x0', '0x001', '0x41', '0x0400', '0x400', '0x7fffffffffffffff']
    }

    def 'test positive string to long'() {
        expect:
        HexValue.toBigInteger(source) == BigInteger.valueOf(target)

        where:
        source << ['0x0', '0x001', '0x41', '0x0400', '0x400', '0x7fffffffffffffff']
        target << [0L, 1L, 65L, 1024L, 1024L, Long.MAX_VALUE]
    }

    def 'test negative string to long'() {
        when:
        HexValue.toBigInteger(source)

        then:
        thrown NumberFormatException

        where:
        source << ['0x', 'ff']
    }

    def 'test positive long to string'() {
        expect:
        HexValue.toString(BigInteger.valueOf(source)) == target

        where:
        source << [0L, 1L, 65L, 1024L, Long.MAX_VALUE]
        target << ['0x0', '0x1', '0x41', '0x400', '0x7fffffffffffffff']
    }

    def 'test negative long to string'() {
        when:
        HexValue.toString(BigInteger.valueOf(source))

        then:
        thrown NumberFormatException

        where:
        source << [-1, Long.MAX_VALUE+1]
    }

    def 'test byte array to string'() {
        expect:
        HexValue.toString(source) == target

        where:
        source << [[1] as byte[], [0xff] as byte[], [0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef] as byte[]]
        target << ['0x01', '0xff', '0x0123456789abcdef']
    }

    def 'test string to byte array'() {
        expect:
        HexValue.toByteArray(source) == target

        where:
        source << ['0x01', '0xff', '0x0123456789abcdef']
        target << [[1] as byte[], [0xff] as byte[], [0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef] as byte[]]
    }

    def 'test round trip'() {
        given: 'the private key'
        String source = '0x37236df251ab70022b1da351f08a20fb52443e37'

        when:
        byte[] byteArray = HexValue.toByteArray(source)

        then:
        byteArray.size() == 20
        byteArray == [0x37, 0x23, 0x6d, 0xf2, 0x51, 0xab, 0x70, 0x02, 0x2b, 0x1d, 0xa3, 0x51, 0xf0, 0x8a, 0x20, 0xfb, 0x52, 0x44, 0x3e, 0x37] as byte[]

        when:
        BigInteger bigInteger = HexValue.toBigInteger(byteArray)

        then:
        bigInteger == 314784596173316724599832564333777983542783131191 as BigInteger

        when:
        byteArray = HexValue.toByteArray(bigInteger, 20)

        then:
        byteArray.size() == 20
        byteArray == [0x37, 0x23, 0x6d, 0xf2, 0x51, 0xab, 0x70, 0x02, 0x2b, 0x1d, 0xa3, 0x51, 0xf0, 0x8a, 0x20, 0xfb, 0x52, 0x44, 0x3e, 0x37] as byte[]

        when:
        bigInteger = HexValue.toBigInteger(source)

        then:
        bigInteger == 314784596173316724599832564333777983542783131191 as BigInteger

        when:
        String string = HexValue.toString(byteArray)

        then:
        string == source

        when:
        string = HexValue.toString(bigInteger)

        then:
        string == source
    }

    def 'test byte string'() {
        expect:
        HexValue.toByteString(byteArray) == HexValue.toByteString(string)
        HexValue.toByteString(byteArray) == HexValue.toByteString(BigInteger.valueOf(bigInteger))
        HexValue.toByteString(BigInteger.valueOf(bigInteger)) == HexValue.toByteString(string)
        HexValue.toString(HexValue.toByteString(byteArray)) == string
        HexValue.toByteArray(HexValue.toByteString(byteArray)) == byteArray
        HexValue.toBigInteger(HexValue.toByteString(byteArray)) == BigInteger.valueOf(bigInteger)

        where:
        byteArray << [[1] as byte[], [0x7f] as byte[], [0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef] as byte[]]
        bigInteger << [1L, 127L, 81985529216486895L]
        string << ['0x01', '0x7f', '0x0123456789abcdef']
    }
}