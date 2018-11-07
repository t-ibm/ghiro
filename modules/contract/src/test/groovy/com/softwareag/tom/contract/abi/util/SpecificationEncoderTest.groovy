/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
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
 * System under specification: {@link SpecificationEncoder}.
 * @author tglaeser
 */
class SpecificationEncoderTest extends Specification {

    def "test an arbitrary contract function"() {
        given: 'an arbitrary contract specification'
        String src = '{"constant":false,"inputs":[{"name":"a","type":"uint32"},{"name":"b","type":"bool"},{"name":"c","type":"uint8[2]"},{"name":"d","type":"uint16[]"}],"name":"foo","outputs":[],"payable":false,"type":"function"}'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(src.bytes, SolidityInterface.SoliditySpecification.class)

        when: 'the function signature is retrieved'
        String signature = SpecificationEncoder.getFunctionSignature(specification)

        then: 'the signature is as expected'
        signature == 'foo(uint32,bool,uint8[2],uint16[])'

        when: 'the parameter count is retrieved'
        int length = specification.getInputParametersSize()

        then: 'its length is as expected'
        length == 5

        when: 'the function id is retrieved'
        String id = SpecificationEncoder.getFunctionId(specification)

        then: 'the id is as expected'
        id == '82fc43b3'

        when: 'the function is encode'
        def values = [BigInteger.ZERO,Boolean.TRUE,[BigInteger.valueOf(2), BigInteger.valueOf(3)],[BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(6)]]
        String result = SpecificationEncoder.encodeParameters(specification, id, values)

        then: 'the returned string is as expected'
        result == (
                '82fc43b3'
                        + '0000000000000000000000000000000000000000000000000000000000000000'
                        + '0000000000000000000000000000000000000000000000000000000000000001'
                        + '0000000000000000000000000000000000000000000000000000000000000002'
                                + '0000000000000000000000000000000000000000000000000000000000000003'
                        + '00000000000000000000000000000000000000000000000000000000000000a0'
                                + '0000000000000000000000000000000000000000000000000000000000000003'
                                        + '0000000000000000000000000000000000000000000000000000000000000004'
                                        + '0000000000000000000000000000000000000000000000000000000000000005'
                                        + '0000000000000000000000000000000000000000000000000000000000000006'
        )

        and: 'the same result is retrieved when executing all steps at once'
        result == specification.encode(values)
    }

    @Unroll def "test an arbitrary contract event #signature"() {
        given: 'a valid contract specification'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)

        expect: 'a valid function signature and id'
        SpecificationEncoder.getEventSignature(specification) == signature
        SpecificationEncoder.getEventId(specification) == id

        where: 'the specification items are from the Console contract'
        source << [
            '{"constant":false,"inputs":[{"name":"a","type":"uint256"},{"name":"b","type":"uint256"}],"name":"Notify","outputs":[],"payable":false,"type":"event"}',
            '{"constant":false,"inputs":[{"name":"a","type":"address"},{"name":"b","type":"uint256"},{"name":"c","type":"uint256"}],"name":"Deposit","outputs":[],"payable":false,"type":"event"}'
        ]
        signature << [
            'Notify(uint256,uint256)',
            'Deposit(uint160,uint256,uint256)', //TODO :: Should be 'Deposit(address,uint256,uint256)'
        ]
        id << [
            '71e71a8458267085d5ab16980fd5f114d2d37f232479c245d523ce8d23ca40ed',
            '019af80c8ede176a6c7a2386598573f3de9c95006daf5403a6fbde3d4bfb9372',
        ]
    }

    @Unroll def "test contract Console.#signature"() {
        given: 'a valid contract specification'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)

        expect: 'a valid function signature and id'
        SpecificationEncoder.getFunctionSignature(specification) == signature
        SpecificationEncoder.getFunctionId(specification) == id

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
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)

        expect: 'a valid function signature and id'
        SpecificationEncoder.getFunctionSignature(specification) == signature
        SpecificationEncoder.getFunctionId(specification) == id

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