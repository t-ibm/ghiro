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
            '{"anonymous":false,"inputs":[{"indexed":true,"name":"a","type":"uint256"},{"indexed":false,"name":"b","type":"uint256"}],"name":"Notify","type":"event"}',
            '{"anonymous":false,"inputs":[{"indexed":true,"name":"a","type":"address"},{"indexed":false,"name":"b","type":"uint256"},{"indexed":false,"name":"c","type":"uint256"}],"name":"Deposit","type":"event"}',
        ]
        signature << [
            'Notify(uint256,uint256)',
            'Deposit(address,uint256,uint256)',
        ]
        id << [
            '71e71a8458267085d5ab16980fd5f114d2d37f232479c245d523ce8d23ca40ed',
            '90890809c654f11d6e72a28fa60149770a0d11ec6c92319d6ceb2bb0a4ea1a15',
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

    def "test function parameter type 'address' vs 'uint160'"() {
        given: 'a valid contract specification'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)
        def values = [BigInteger.ONE.shiftLeft(ValueBase.ADDRESS_LENGTH).subtract(BigInteger.ONE), BigInteger.valueOf(42)]

        expect: 'a valid function signature and id'
        SpecificationEncoder.getFunctionSignature(specification) == signature
        SpecificationEncoder.getFunctionId(specification) == id
        SpecificationEncoder.encodeParameters(specification, id, values) == result

        where: 'the specification items are from the Console contract'
        source << [
            '{"constant":false,"inputs":[{"name":"payee","type":"uint160"},{"name":"amount","type":"uint256"}],"name":"sendTo","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function"}',
            '{"constant":false,"inputs":[{"name":"payee","type":"address"},{"name":"amount","type":"uint256"}],"name":"sendTo","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function"}',
        ]
        signature << [
            'sendTo(uint160,uint256)',
            'sendTo(uint160,uint256)',
        ]
        id << [
            'f57c30b2',
            'f57c30b2',
        ]
        result << [
            'f57c30b2'
                + '000000000000000000000000ffffffffffffffffffffffffffffffffffffffff'
                + '000000000000000000000000000000000000000000000000000000000000002a',
            'f57c30b2'
                + '000000000000000000000000ffffffffffffffffffffffffffffffffffffffff'
                + '000000000000000000000000000000000000000000000000000000000000002a',
        ]
    }

    def "test event parameter type 'address' vs 'uint160'"() {
        given: 'a valid contract specification'
        SolidityInterface.SoliditySpecification specification = ObjectMapperFactory.getJsonMapper().readValue(source.bytes, SolidityInterface.SoliditySpecification.class)

        expect: 'a valid function signature and id'
        SpecificationEncoder.getEventSignature(specification) == signature
        SpecificationEncoder.getEventId(specification) == id

        where: 'the specification items are from the Console contract'
        source << [
            '{"anonymous":false,"inputs":[{"indexed":true,"name":"payee","type":"address"},{"indexed":false,"name":"amount","type":"uint256"},{"indexed":false,"name":"balance","type":"uint256"}],"name":"Sent","type":"event"}',
            '{"anonymous":false,"inputs":[{"indexed":true,"name":"payee","type":"uint160"},{"indexed":false,"name":"amount","type":"uint256"},{"indexed":false,"name":"balance","type":"uint256"}],"name":"Sent","type":"event"}',
        ]
        signature << [
            'Sent(address,uint256,uint256)',
            'Sent(uint160,uint256,uint256)',
        ]
        id << [
            '6356739d963da01dc3533acba7203430fcc14f2175d48a8dd0973d7db49c785e',
            '318900a757c479f66836672cd876f43b1a3fd4161a8710f20b9b4c108ed84968',
        ]
    }
}