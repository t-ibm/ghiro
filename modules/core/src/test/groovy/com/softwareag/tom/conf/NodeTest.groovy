/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.conf

import spock.lang.Specification

/**
 * System under specification: {@link Node}.
 * @author tglaeser
 */
class NodeTest extends Specification {

    def "test configuration variants"() {
        when: 'a default node configuration instance is requested'
        Node node = Node.instance()

        then: 'the default configuration values are retrieved'
        node.name == 'default'
        node.host.ip == '127.0.0.1'
        node.contract.registry.location == URI.create('../../modules/contract/build/solc')
        node.environments.size() == 1

        when: 'a non existing named node configuration instance is requested'
        node = Node.instance('does-not-exist')

        then: 'the default configuration values are retrieved'
        node.name == 'default'
        node.host.ip == '127.0.0.1'
        node.contract.registry.location == URI.create('../../modules/contract/build/solc')

        when: 'a named node configuration instance is requested'
        node = Node.instance('production')

        then: 'non default configuration values are retrieved for the set values'
        node.name == 'production'
        node.host.ip == '127.0.0.1'
        node.contract.registry.location == URI.create('../../../../../../../tom/ghiro/modules/contract/build/solc')
    }
}