/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.util

import com.softwareag.tom.ObjectMapperFactory
import com.softwareag.tom.contract.abi.sol.SolidityInterface
import com.softwareag.tom.util.HexValueBase
import spock.lang.Specification
import spock.lang.Unroll

/**
 * System under specification: {@link ValueEncoder}.
 * @author tglaeser
 */
class ValueEncoderTest extends Specification {

    def "test unsupported operation exception"() {
        given: 'a parameter of type "bool"'
        String source = '{"name":"a","type":"bool"}'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        when: 'a value is mapped to a parameter of incompatible type'
        ValueEncoder.encode(parameter.type, "NotABooleanValue")

        then: 'an exception is thrown'
        def e = thrown(UnsupportedOperationException)
        e.cause == null
        e.message == "Value of type 'class java.lang.String' cannot be encoded as 'bool'."
    }

    @Unroll def "test bool parameter with value #value"() {
        given: 'a parameter of type "bool"'
        String source = '{"name":"a","type":"bool"}'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == result

        where: 'the given value is valid'
        value << [
                Boolean.FALSE,
                Boolean.TRUE,
        ]
        result << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000001',
        ]
    }

    def "test address parameter"() {
        given: 'a parameter of type "address" and a matching value'
        String source = '{"name":"a","type":"address"}'
        BigInteger value = HexValueBase.toBigInteger('0xbe5422d15f39373eb0a97ff8c10fbd0e40e29338')
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == '000000000000000000000000be5422d15f39373eb0a97ff8c10fbd0e40e29338'
    }

    @Unroll def "test uint<M> parameter with value #value"() {
        given: 'a parameter of type "uint<M>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == result

        where: 'the given value is valid'
        source << [
                '{"name":"a","type":"uint64"}',
                '{"name":"b","type":"uint64"}',
                '{"name":"c","type":"uint"}',
                '{"name":"d","type":"uint"}',
        ]
        value << [
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16),
                new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe", 16),
        ]
        result << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000007fffffffffffffff',
                'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
                'fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe',
        ]
    }

    @Unroll def "test int<M> parameter with value #value"() {
        given: 'a parameter of type "int<M>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == result

        where: 'the given value is valid'
        source << [
                '{"name":"a","type":"int64"}',
                '{"name":"b","type":"int64"}',
                '{"name":"c","type":"int64"}',
                '{"name":"d","type":"uint"}',
        ]
        value << [
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                BigInteger.valueOf(Long.MIN_VALUE),
                BigInteger.valueOf(-1),
        ]
        result << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000007fffffffffffffff',
                'ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000',
                'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
        ]
    }

    @Unroll def "test ufixed<M>x<N> parameter with value #value"() {
        given: 'a parameter of type "ufixed<M>x<N>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == result

        where: 'the given value is valid'
        source << [
                '{"name":"a","type":"ufixed24x40"}',
                '{"name":"b","type":"ufixed24x40"}',
                '{"name":"c","type":"ufixed"}',
        ]
        value << [
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16),
        ]
        result << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000007fffffffffffffff',
                'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
        ]
    }

    @Unroll def "test fixed<M>x<N> parameter with value #value"() {
        given: 'a parameter of type "fixed<M>x<N>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == result

        where: 'the given value is valid'
        source << [
                '{"name":"a","type":"fixed24x40"}',
                '{"name":"b","type":"fixed24x40"}',
                '{"name":"c","type":"fixed24x40"}',
                '{"name":"d","type":"fixed24x40"}',
        ]
        value << [
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                BigInteger.valueOf(Long.MIN_VALUE),
                BigInteger.valueOf(-1),
        ]
        result << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000007fffffffffffffff',
                'ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000',
                'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
        ]
    }

    @Unroll def "test fixed-length bytes<M> parameter with value #value"() {
        given: 'a parameter of type "bytes<M>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == result

        where: 'the given value is valid'
        source << [
                '{"name":"a","type":"bytes6"}',
                '{"name":"b","type":"bytes1"}',
                '{"name":"c","type":"bytes4"}',
        ]
        value << [
                [0, 1, 2, 3, 4, 5 ] as byte[],
                [0] as byte[],
                "dave".bytes,
        ]
        result << [
                '0001020304050000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000000',
                '6461766500000000000000000000000000000000000000000000000000000000',
        ]
    }

    @Unroll def "test dynamic bytes parameter with value #value"() {
        given: 'a parameter of type "bytes"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == result

        where: 'the given value is valid'
        source << [
                '{"name":"a","type":"bytes"}',
                '{"name":"b","type":"bytes"}',
                '{"name":"c","type":"bytes"}',
                '{"name":"d","type":"bytes"}',
        ]
        value << [
                [0, 1, 2, 3, 4, 5 ] as byte[],
                [0] as byte[],
                'dave'.bytes,
                ('Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod '
                        + 'tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim '
                        + 'veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex '
                        + 'ea commodo consequat. Duis aute irure dolor in reprehenderit in '
                        + 'voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur '
                        + 'sint occaecat cupidatat non proident, sunt in culpa qui officia '
                        + 'deserunt mollit anim id est laborum.').bytes
        ]
        result << [
                '0000000000000000000000000000000000000000000000000000000000000006'
                        + "0001020304050000000000000000000000000000000000000000000000000000",
                '0000000000000000000000000000000000000000000000000000000000000001'
                        + "0000000000000000000000000000000000000000000000000000000000000000",
                '0000000000000000000000000000000000000000000000000000000000000004'
                        + "6461766500000000000000000000000000000000000000000000000000000000",
                '00000000000000000000000000000000000000000000000000000000000001bd'
                        + '4c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e73'
                        + '656374657475722061646970697363696e6720656c69742c2073656420646f20'
                        + '656975736d6f642074656d706f7220696e6369646964756e74207574206c6162'
                        + '6f726520657420646f6c6f7265206d61676e6120616c697175612e2055742065'
                        + '6e696d206164206d696e696d2076656e69616d2c2071756973206e6f73747275'
                        + '6420657865726369746174696f6e20756c6c616d636f206c61626f726973206e'
                        + '69736920757420616c697175697020657820656120636f6d6d6f646f20636f6e'
                        + '7365717561742e2044756973206175746520697275726520646f6c6f7220696e'
                        + '20726570726568656e646572697420696e20766f6c7570746174652076656c69'
                        + '7420657373652063696c6c756d20646f6c6f726520657520667567696174206e'
                        + '756c6c612070617269617475722e204578636570746575722073696e74206f63'
                        + '63616563617420637570696461746174206e6f6e2070726f6964656e742c2073'
                        + '756e7420696e2063756c706120717569206f666669636961206465736572756e'
                        + '74206d6f6c6c697420616e696d20696420657374206c61626f72756d2e000000'
        ]
    }

    def "test string parameter"() {
        given: 'a parameter of type "string" and a matching value'
        String source = '{"name":"a","type":"string"}'
        String value = "Hello, world!"
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == ('000000000000000000000000000000000000000000000000000000000000000d'
                + '48656c6c6f2c20776f726c642100000000000000000000000000000000000000')
    }

    def "test fixed-length array parameter"() {
        given: 'a parameter of type "<type>[M]" and a matching value'
        String source = '{"name":"a","type":"uint[3]"}'
        List<BigInteger> value = [BigInteger.ONE,BigInteger.valueOf(2),BigInteger.valueOf(3)]
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == (
                '0000000000000000000000000000000000000000000000000000000000000001'
                        + '0000000000000000000000000000000000000000000000000000000000000002'
                        + '0000000000000000000000000000000000000000000000000000000000000003'
        )
    }

    def "test single item array parameter"() {
        given: 'a parameter of type "<type>[M]" and a matching value'
        String source = '{"name":"a","type":"uint[1]"}'
        List<BigInteger> value = [BigInteger.ONE]
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == '0000000000000000000000000000000000000000000000000000000000000001'
    }

    def "test dynamic array parameter"() {
        given: 'a parameter of type "<type>[]" and a matching value'
        String source = '{"name":"a","type":"uint[]"}'
        List<BigInteger> value = [BigInteger.ONE,BigInteger.valueOf(2),BigInteger.valueOf(3)]
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == (
                '0000000000000000000000000000000000000000000000000000000000000003'
                        + '0000000000000000000000000000000000000000000000000000000000000001'
                        + '0000000000000000000000000000000000000000000000000000000000000002'
                        + '0000000000000000000000000000000000000000000000000000000000000003'
        )
    }

    def "test empty array parameter"() {
        given: 'a parameter of type "<type>[]" and a matching value'
        String source = '{"name":"a","type":"uint[]"}'
        List<BigInteger> value = []
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid result'
        ValueEncoder.encode(parameter.type, value) == '0000000000000000000000000000000000000000000000000000000000000000'
    }
}