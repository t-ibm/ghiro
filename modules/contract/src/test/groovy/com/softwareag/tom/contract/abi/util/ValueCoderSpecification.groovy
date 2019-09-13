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
 * Systems under specification: {@link ValueEncoder} and {@link ValueDecoder}.
 * @author tglaeser
 */
class ValueCoderSpecification extends Specification {

    @Unroll def "test negative encode value #value"() {
        given: 'a parameter of type "bool"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        when: 'a value is mapped to a parameter of incompatible type'
        ValueEncoder.encode(parameter.type, value)

        then: 'an exception is thrown'
        def e = thrown(UnsupportedOperationException)
        e.cause == null
        e.message == message

        where: 'the given values are invalid'
        source << [
                '{"name":"a","type":"bool"}',
                '{"name":"b","type":"foo"}',
        ]
        value << [
                "NotABooleanValue",
                "bar",
        ]
        message << [
                "Value of type 'class java.lang.String' cannot be encoded as 'bool'.",
                "Value of type 'class java.lang.String' cannot be encoded as '${ParameterTypeJava.UNKNOWN.name}'.",
        ]
    }

    @Unroll def "test negative decode value #value"() {
        given: 'a parameter of type "bool"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        when: 'a value is mapped to a parameter of incompatible type'
        ValueDecoder.decode(parameter.type, value)

        then: 'an exception is thrown'
        def e = thrown(UnsupportedOperationException)
        e.cause == null
        e.message == message

        where: 'the given values are invalid'
        source << [
                '{"name":"a","type":"foo"}',
                '{"name":"b","type":"uint[0]"}',
                '{"name":"c","type":"uint[][]"}',
        ]
        value << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000000',
        ]
        message << [
                "Unknown type, value '$value' cannot be decoded.",
                "Zero length fixed array is invalid type.",
                "Arrays of arrays are not currently supported for external functions, see http://solidity.readthedocs.io/en/develop/types.html#members.",
        ]
    }

    @Unroll def "test bool parameter with value #javaValue"() {
        given: 'a parameter of type "bool"'
        String source = '{"name":"a","type":"bool"}'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type == ParameterTypeJava.BOOL
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue

        where: 'the given values are valid'
        javaValue << [
                Boolean.FALSE,
                Boolean.TRUE,
        ]
        hexValue << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000001',
        ]
    }

    @Unroll def "test bool parameter with offset #offset and value #javaValue"() {
        given: 'a parameter of type "bool"'
        String source = '{"name":"a","type":"bool"}'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        ValueDecoder.decode(parameter.type, hexValue, offset) == javaValue

        where: 'the given value is valid'
        offset << [
                64,
                64,
        ]
        javaValue << [
                Boolean.FALSE,
                Boolean.TRUE,
        ]
        hexValue << [
                '0000000000000000000000000000000000000000000000007fffffffffffffff'
                        + '0000000000000000000000000000000000000000000000000000000000000000'
                        + '0000000000000000000000000000000000000000000000007fffffffffffffff',
                '0000000000000000000000000000000000000000000000007fffffffffffffff'
                        + '0000000000000000000000000000000000000000000000000000000000000001'
                        + '0000000000000000000000000000000000000000000000007fffffffffffffff',
        ]
    }

    def "test address parameter"() {
        given: 'a parameter of type "address" and matching values'
        String source = '{"name":"a","type":"address"}'
        String hexValue = '000000000000000000000000be5422d15f39373eb0a97ff8c10fbd0e40e29338'
        BigInteger javaValue = HexValueBase.toBigInteger('0xbe5422d15f39373eb0a97ff8c10fbd0e40e29338')
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type == ParameterTypeJava.ADDRESS
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue
    }

    @Unroll def "test uint<M> parameter with value #javaValue"() {
        given: 'a parameter of type "uint<M>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.NumericType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue

        where: 'the given values are valid'
        source << [
                '{"name":"a","type":"uint64"}',
                '{"name":"b","type":"uint64"}',
                '{"name":"c","type":"uint"}',
                '{"name":"d","type":"uint"}',
        ]
        javaValue << [
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16),
                new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe", 16),
        ]
        hexValue << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000007fffffffffffffff',
                'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
                'fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe',
        ]
    }

    @Unroll def "test int<M> parameter with value #javaValue"() {
        given: 'a parameter of type "int<M>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.NumericType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue

        where: 'the given values are valid'
        source << [
                '{"name":"a","type":"int64"}',
                '{"name":"b","type":"int64"}',
                '{"name":"c","type":"int64"}',
                '{"name":"d","type":"int"}',
        ]
        javaValue << [
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                BigInteger.valueOf(Long.MIN_VALUE),
                BigInteger.valueOf(-1),
        ]
        hexValue << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000007fffffffffffffff',
                'ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000',
                'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
        ]
    }

    @Unroll def "test ufixed<M>x<N> parameter with value #javaValue"() {
        given: 'a parameter of type "ufixed<M>x<N>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.NumericType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue

        where: 'the given values are valid'
        source << [
                '{"name":"a","type":"ufixed"}',
                '{"name":"b","type":"ufixed128x19"}',
                '{"name":"c","type":"ufixed24x40"}',
                '{"name":"d","type":"ufixed24x40"}',
                '{"name":"e","type":"ufixed128x128"}',
        ]
        javaValue << [
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16),
        ]
        hexValue << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000007fffffffffffffff',
                'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
        ]
    }

    @Unroll def "test fixed<M>x<N> parameter with value #javaValue"() {
        given: 'a parameter of type "fixed<M>x<N>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.NumericType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue

        where: 'the given values are valid'
        source << [
                '{"name":"a","type":"fixed"}',
                '{"name":"b","type":"fixed128x19"}',
                '{"name":"c","type":"fixed24x40"}',
                '{"name":"d","type":"fixed24x40"}',
                '{"name":"e","type":"fixed24x40"}',
                '{"name":"f","type":"fixed24x40"}',
        ]
        javaValue << [
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                BigInteger.valueOf(Long.MIN_VALUE),
                BigInteger.valueOf(-1),
        ]
        hexValue << [
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000007fffffffffffffff',
                'ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000',
                'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
        ]
    }

    @Unroll def "test fixed-length bytes<M> parameter with value #javaValue"() {
        given: 'a parameter of type "bytes<M>"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.BytesType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue

        where: 'the given values are valid'
        source << [
                '{"name":"a","type":"bytes6"}',
                '{"name":"b","type":"bytes1"}',
                '{"name":"c","type":"bytes4"}',
                '{"name":"d","type":"bytes0"}',
        ]
        javaValue << [
                [0, 1, 2, 3, 4, 5 ] as byte[],
                [0] as byte[],
                "dave".bytes,
                [] as byte[],
        ]
        hexValue << [
                '0001020304050000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000000',
                '6461766500000000000000000000000000000000000000000000000000000000',
                '',
        ]
    }

    @Unroll def "test dynamic bytes parameter with source #source"() {
        given: 'a parameter of type "bytes"'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type == ParameterTypeJava.BYTES
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue

        where: 'the given values are valid'
        source << [
                '{"name":"a","type":"bytes"}',
                '{"name":"b","type":"bytes"}',
                '{"name":"c","type":"bytes"}',
                '{"name":"d","type":"bytes"}',
        ]
        javaValue << [
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
        hexValue << [
                '0000000000000000000000000000000000000000000000000000000000000006'
                        + '0001020304050000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000001'
                        + '0000000000000000000000000000000000000000000000000000000000000000',
                '0000000000000000000000000000000000000000000000000000000000000004'
                        + '6461766500000000000000000000000000000000000000000000000000000000',
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
        given: 'a parameter of type "string" and matching values'
        String source = '{"name":"a","type":"string"}'
        String javaValue = 'Hello, world!'
        String hexValue = (
                '000000000000000000000000000000000000000000000000000000000000000d'
                        + '48656c6c6f2c20776f726c642100000000000000000000000000000000000000'
        )
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type == ParameterTypeJava.STRING
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue
    }

    def "test fixed-length array parameter"() {
        given: 'a parameter of type "<type>[M]" and matching values'
        String source = '{"name":"a","type":"uint[3]"}'
        List<BigInteger> javaValue = [BigInteger.ONE,BigInteger.valueOf(2),BigInteger.valueOf(3)]
        String hexValue = (
                '0000000000000000000000000000000000000000000000000000000000000001'
                        + '0000000000000000000000000000000000000000000000000000000000000002'
                        + '0000000000000000000000000000000000000000000000000000000000000003'
        )
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.ArrayType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue
    }

    def "test single item array parameter"() {
        given: 'a parameter of type "<type>[M]" and matching values'
        String source = '{"name":"a","type":"uint[1]"}'
        List<BigInteger> javaValue = [BigInteger.ONE]
        String hexValue = '0000000000000000000000000000000000000000000000000000000000000001'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.ArrayType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue
    }

    def "test dynamic array parameter"() {
        given: 'a parameter of type "<type>[]" and a matching values'
        String source = '{"name":"a","type":"uint[]"}'
        List<BigInteger> javaValue = [BigInteger.ONE,BigInteger.valueOf(2),BigInteger.valueOf(3)]
        String hexValue = (
                '0000000000000000000000000000000000000000000000000000000000000003'
                        + '0000000000000000000000000000000000000000000000000000000000000001'
                        + '0000000000000000000000000000000000000000000000000000000000000002'
                        + '0000000000000000000000000000000000000000000000000000000000000003'
        )
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.ArrayType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue
    }

    def "test empty array parameter"() {
        given: 'a parameter of type "<type>[]" and matching values'
        String source = '{"name":"a","type":"uint[]"}'
        List<BigInteger> javaValue = []
        String hexValue = '0000000000000000000000000000000000000000000000000000000000000000'
        SolidityInterface.SolidityParameter parameter = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SolidityParameter.class)

        expect: 'a valid conversion'
        parameter.type instanceof ParameterTypeJava.ArrayType
        parameter.type.asType(javaValue) == javaValue
        ValueEncoder.encode(parameter.type, javaValue) == hexValue
        ValueDecoder.decode(parameter.type, hexValue) == javaValue
    }
}