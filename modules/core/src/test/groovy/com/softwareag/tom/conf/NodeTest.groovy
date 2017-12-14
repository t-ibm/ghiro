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

    def "test default configuration"() {
        given: 'a default node configuration instance'
        Node node = Node.instance()

        expect: 'the default configuration values'
        node.name == 'default'
        node.contract.registry.location == URI.create('../../modules/contract/build/solc')
    }
}