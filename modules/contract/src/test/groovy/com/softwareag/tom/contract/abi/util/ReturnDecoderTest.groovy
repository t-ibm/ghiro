/*
 * Copyright (c) 2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.util

import com.softwareag.tom.ObjectMapperFactory
import com.softwareag.tom.contract.abi.sol.SolidityInterface
import spock.lang.Specification
import spock.lang.Unroll

/**
 * System under specification: {@link ReturnDecoder}.
 * @author tglaeser
 */
class ReturnDecoderTest extends Specification {

    @Unroll def "test contract function with single return #javaValues"() {
        given: 'a valid contract specification'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)

        expect: 'a valid list of return values'
        ReturnDecoder.decode(specification.getOutputParameters(), hexValue) == javaValues

        where: 'contract is valid and the returned hex value matches the expected Java values'
        source << [
                '{"constant":false,"inputs":[],"name":"foo","outputs":[{"name":"a","type":"uint"}],"payable":false,"type":"function"}',
                '{"constant":false,"inputs":[],"name":"bar","outputs":[{"name":"a","type":"string"}],"payable":false,"type":"function"}',
                '{"constant":false,"inputs":[],"name":"baz","outputs":[{"name":"a","type":"string"}],"payable":false,"type":"function"}',
        ]
        javaValues << [
                [BigInteger.valueOf(55)],
                ['one more time'],
                [''],
        ]
        hexValue << [
                '0x0000000000000000000000000000000000000000000000000000000000000037',
                '0x0000000000000000000000000000000000000000000000000000000000000020'
                        + '000000000000000000000000000000000000000000000000000000000000000d'
                        + '6f6e65206d6f72652074696d6500000000000000000000000000000000000000',
                '0x0000000000000000000000000000000000000000000000000000000000000020'
                        + '0000000000000000000000000000000000000000000000000000000000000000'
        ]
    }

    @Unroll def "test contract function with multiple returns #javaValues"() {
        given: 'a valid contract specification'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)

        expect: 'a valid list of return values'
        ReturnDecoder.decode(specification.getOutputParameters(), hexValue) == javaValues

        where: 'contract is valid and the returned hex value matches the expected Java values'
        source << [
                '{"constant":false,"inputs":[],"name":"foo","outputs":[{"name":"a","type":"uint"},{"name":"b","type":"uint"}],"payable":false,"type":"function"}',
                '{"constant":false,"inputs":[],"name":"bar","outputs":[{"name":"a","type":"string"},{"name":"b","type":"string"},{"name":"c","type":"string"},{"name":"d","type":"string"}],"payable":false,"type":"function"}',
        ]
        javaValues << [
                [BigInteger.valueOf(55), BigInteger.valueOf(7)],
                ['def1', 'ghi1', 'jkl1', 'mno2'],
        ]
        hexValue << [
                '0x0000000000000000000000000000000000000000000000000000000000000037'
                        + '0000000000000000000000000000000000000000000000000000000000000007',
                '0x0000000000000000000000000000000000000000000000000000000000000080'
                        + '00000000000000000000000000000000000000000000000000000000000000c0'
                        + '0000000000000000000000000000000000000000000000000000000000000100'
                        + '0000000000000000000000000000000000000000000000000000000000000140'
                        + '0000000000000000000000000000000000000000000000000000000000000004'
                        + '6465663100000000000000000000000000000000000000000000000000000000'
                        + '0000000000000000000000000000000000000000000000000000000000000004'
                        + '6768693100000000000000000000000000000000000000000000000000000000'
                        + '0000000000000000000000000000000000000000000000000000000000000004'
                        + '6a6b6c3100000000000000000000000000000000000000000000000000000000'
                        + '0000000000000000000000000000000000000000000000000000000000000004'
                        + '6d6e6f3200000000000000000000000000000000000000000000000000000000',
        ]
    }

    def "test contract function with static array return"() {
        given: 'a valid contract and the returned hex value matches the expected Java values'
        String src = '{"constant":false,"inputs":[],"name":"foo","outputs":[{"name":"a","type":"uint256[3]"}],"payable":false,"type":"function"}'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(src.bytes, SolidityInterface.SoliditySpecification.class)
        String hexValue = (
                '0x0000000000000000000000000000000000000000000000000000000000000037'
                        + '0000000000000000000000000000000000000000000000000000000000000001'
                        + '000000000000000000000000000000000000000000000000000000000000000a'
        )
        List javaValues = [[BigInteger.valueOf(55), BigInteger.ONE, BigInteger.TEN]]

        expect: 'a valid list of return values'
        ReturnDecoder.decode(specification.getOutputParameters(), hexValue) == javaValues
    }

    def "test contract function with void return"() {
        given: 'a valid contract and the returned hex value matches the expected Java values'
        String src = '{"constant":false,"inputs":[],"name":"foo","outputs":[],"payable":false,"type":"function"}'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(src.bytes, SolidityInterface.SoliditySpecification.class)
        String hexValue = '0x'
        List javaValues = []

        expect: 'a valid list of return values'
        ReturnDecoder.decode(specification.getOutputParameters(), hexValue) == javaValues
    }

    def "test contract function with empty return"() {
        given: 'a valid contract and the returned hex value matches the expected Java values'
        String src = '{"constant":false,"inputs":[],"name":"foo","outputs":[{"name":"a","type":"uint"}],"payable":false,"type":"function"}'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(src.bytes, SolidityInterface.SoliditySpecification.class)
        String hexValue = '0x'
        List javaValues = []

        expect: 'a valid list of return values'
        ReturnDecoder.decode(specification.getOutputParameters(), hexValue) == javaValues
    }
}