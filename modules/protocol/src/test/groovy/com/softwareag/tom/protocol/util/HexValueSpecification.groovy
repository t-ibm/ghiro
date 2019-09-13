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
class HexValueSpecification extends Specification {

    def 'test unformatted byte string data'() {
        expect:
        HexValue.toByteString(byteArray) == HexValue.toByteString(string)
        HexValue.toString(HexValue.toByteString(byteArray)) == string
        HexValue.toByteArray(HexValue.toByteString(byteArray)) == byteArray

        where:
        byteArray << [[1] as byte[], [0x7f] as byte[], [0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef] as byte[]]
        string << ['0x01', '0x7f', '0x0123456789abcdef']
    }

    def 'test byte string quantities'() {
        expect:
        HexValue.toByteString(BigInteger.valueOf(bigInteger)) == HexValue.toByteString(string)
        HexValue.toString(HexValue.toByteString(bigInteger)) == string
        HexValue.toBigInteger(HexValue.toByteString(bigInteger)) == BigInteger.valueOf(bigInteger)

        where:
        bigInteger << [1L, 127L, 81985529216486895L]
        string << ['0x1', '0x7f', '0x123456789abcdef']
    }
}