/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.abi

import com.softwareag.tom.abi.sol.SolidityInterface
import com.softwareag.tom.abi.sol.SolidityLocationFileSystem
import com.softwareag.tom.extension.Node
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link ContractRegistryLocation}.
 * @author tglaeser
 */
class ContractRegistryLocationTest extends Specification {

    @Shared @Node protected ConfigObject config
    @Shared protected ContractRegistry contractRegistry

    def setup() {
        given: 'a fs contract registry'
        contractRegistry = ContractRegistry.build(new SolidityLocationFileSystem("${config.node.contract.registry.location}"));
    }

    def "test contract registry"() {
        when: 'the contracts are loaded into memory'
        def  contracts = contractRegistry.load()

        then: 'the contracts are accessible'
        contracts.size() > 0
        contracts['sample/util/Console'].contractBinary.startsWith('6060604052341561000c57fe5b5b')
        contracts['sample/SimpleStorage'].contractBinary.startsWith('6060604052341561000c57fe5b5b')

        when: 'the list of specifications are queried'
        ContractInterface contractInterface = contracts['sample/util/Console'].contractAbi
        List constructors = contractInterface.constructors as List<SolidityInterface.SoliditySpecification>
        List functions = contractInterface.functions as List<SolidityInterface.SoliditySpecification>
        List events = contractInterface.events as List<SolidityInterface.SoliditySpecification>

        then: 'the expected number of specification types and there values are as expected'
        //Constructors
        constructors.size() == 0
        //Functions
        functions.size() == 3
        functions.get(0).name == 'log'
        functions.get(1).name == 'uintToBytes'
        functions.get(1).type == 'function'
        functions.get(1).inputParameters.get(0).name == 'v'
        functions.get(1).inputParameters.get(0).type.name == 'uint256'
        functions.get(1).inputParameters.get(0).type.type == BigInteger.class
        functions.get(1).outputParameters.get(0).name == 'ret'
        functions.get(1).outputParameters.get(0).type.name == 'bytes32'
        functions.get(1).constant
        !functions.get(1).payable
        functions.get(2).name == 'log'
        //Events
        events.size() == 2
        events.get(0).name == 'LogAddress'
        !events.get(0).anonymous
        !events.get(0).inputParameters.get(0).indexed
        events.get(1).name == 'LogUint'

        when: 'the list of specifications are queried'
        contractInterface = contracts['sample/SimpleStorage'].contractAbi
        constructors = contractInterface.constructors as List<SolidityInterface.SoliditySpecification>
        functions = contractInterface.functions as List<SolidityInterface.SoliditySpecification>
        events = contractInterface.events as List<SolidityInterface.SoliditySpecification>

        then: 'the expected number of specification types and there values are as expected'
        //Constructors
        contractInterface.constructors.size() == 1
        constructors.get(0).name == null
        //Functions
        functions.size() == 6
        functions.get(0).name == 'storedData'
        functions.get(1).name == 'log'
        functions.get(2).name == 'set'
        functions.get(3).name == 'get'
        functions.get(4).name == 'uintToBytes'
        functions.get(5).name == 'log'
        //Events
        events.size() == 2
        events.get(0).name == 'LogAddress'
        events.get(1).name == 'LogUint'
    }
}