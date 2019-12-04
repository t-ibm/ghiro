/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.conf

import spock.lang.Specification

import java.nio.file.Paths

/**
 * System under specification: {@link Node}.
 * @author tglaeser
 */
class NodeSpecification extends Specification {

    def "test configuration variants"() {
        when: 'a default node configuration instance is requested'
        Node node = Node.instance()

        then: 'the default configuration values are retrieved'
        node.name == 'default'
        node.host.ip == '127.0.0.1'
        node.host.grpc.port == 10997
        node.host.info.port == 26658
        node.host.web3.port == 26660
        node.host.tendermint.port == 26656
        node.key.private == '9505E4785FF66E23D8B1ECB47A1E49AA01D81C19' //TODO :: Retrieve from burrow.toml instead
        node.contract.registry.locationAsUri == Paths.get('../../modules/contract/build/solidity/test').toUri().normalize()
        node.size() == 1

        when: 'a non existing named node configuration instance is requested'
        node = Node.instance('does-not-exist')

        then: 'the default configuration values are retrieved'
        node.name == 'default'
        node.host.ip == '127.0.0.1'
        node.contract.registry.locationAsUri == Paths.get('../../modules/contract/build/solidity/test').toUri().normalize()

        when: 'a named node configuration instance is requested'
        node = Node.instance('production')

        then: 'non default configuration values are retrieved for the set values'
        node.name == 'production'
        node.host.ip == '127.0.0.1'
        node.contract.registry.locationAsUri == Paths.get('../../../../../../../../tom/ghiro/modules/contract/build/solidity/main').toUri().normalize()
    }
}