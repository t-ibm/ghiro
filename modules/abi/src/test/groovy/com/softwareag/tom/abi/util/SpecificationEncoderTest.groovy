/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.abi.util

import com.softwareag.tom.ObjectMapperFactory
import com.softwareag.tom.abi.sol.SolidityInterface
import spock.lang.Specification
import spock.lang.Unroll

/**
 * System under specification: {@link SpecificationEncoder}.
 * @author tglaeser
 */
class SpecificationEncoderTest extends Specification {

    def "test an arbitrary contract function"() {
        given: 'an arbitrary contract specification'
        String src = '{"constant":false,"inputs":[{"name":"a","type":"uint32"},{"name":"b","type":"bool"},{"name":"c","type":"uint8[42]"},{"name":"d","type":"uint16[]"}],"name":"foo","outputs":[],"payable":false,"type":"function"}'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getObjectMapper().readValue(src.bytes, SolidityInterface.SoliditySpecification.class)

        when: 'the function signature is retrieved'
        String signature = SpecificationEncoder.getFunctionSignature(specification.getName(), specification.getInputParameters())

        then: 'the signature is as expected'
        signature == 'foo(uint32,bool,uint8[42],uint16[])'

        when: 'the function signature is retrieved'
        int length = SpecificationEncoder.getParameterCount(specification.getInputParameters())

        then: 'the signature is as expected'
        length == 45

        when: 'the function id is retrieved'
        String id = SpecificationEncoder.getFunctionId(signature)

        then: 'the id is as expected'
        id == '0b6d38a1'
    }

    @Unroll def "test contract Console.#signature"() {
        given: 'a valid contract specification'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)

        expect: 'a valid function signature and id'
        SpecificationEncoder.getFunctionSignature(specification.getName(), specification.getInputParameters()) == signature
        SpecificationEncoder.getFunctionId(signature) == id

        where: 'the specification items are from the Console contract'
        source << [
                '{"constant":false,"inputs":[],"name":"log","outputs":[],"payable":false,"type":"function"}',
                '{"constant":true,"inputs":[{"name":"v","type":"uint256"}],"name":"uintToBytes","outputs":[{"name":"ret","type":"bytes32"}],"payable":false,"type":"function"}',
                '{"constant":false,"inputs":[{"name":"x","type":"uint256"}],"name":"log","outputs":[],"payable":false,"type":"function"}',

        ]
        signature << [
                'log()',
                'uintToBytes(uint256)',
                'log(uint256)',
        ]
        id << [
                '51973ec9',
                '94e8767d',
                'f82c50f1',
        ]
    }

    @Unroll def "test contract SimpleStorage.#signature"() {
        given: 'a valid contract specification'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getObjectMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)

        expect: 'a valid function signature and id'
        SpecificationEncoder.getFunctionSignature(specification.getName(), specification.getInputParameters()) == signature
        SpecificationEncoder.getFunctionId(signature) == id

        where: 'the specification items are from the SimpleStorage contract'
        source << [
                '{"constant":true,"inputs":[],"name":"storedData","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"}',
                '{"constant":false,"inputs":[],"name":"log","outputs":[],"payable":false,"type":"function"}',
                '{"constant":false,"inputs":[{"name":"p","type":"uint256"}],"name":"set","outputs":[],"payable":false,"type":"function"}',
                '{"constant":true,"inputs":[],"name":"get","outputs":[{"name":"r","type":"uint256"}],"payable":false,"type":"function"}',
                '{"constant":true,"inputs":[{"name":"v","type":"uint256"}],"name":"uintToBytes","outputs":[{"name":"ret","type":"bytes32"}],"payable":false,"type":"function"}',
                '{"constant":false,"inputs":[{"name":"x","type":"uint256"}],"name":"log","outputs":[],"payable":false,"type":"function"}',

        ]
        signature << [
                'storedData()',
                'log()',
                'set(uint256)',
                'get()',
                'uintToBytes(uint256)',
                'log(uint256)',
        ]
        id << [
                '2a1afcd9',
                '51973ec9',
                '60fe47b1',
                '6d4ce63c',
                '94e8767d',
                'f82c50f1',
        ]
    }
}