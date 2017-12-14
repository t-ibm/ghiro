package com.softwareag.tom.conf

import spock.lang.Specification

class NodeTest extends Specification {

    def "test default configuration"() {
        given: 'a parameter of type "bool"'
        Node node = Node.instance()

        expect: 'the default configuration'
        node.name == 'default'
        node.contract.registry.location == URI.create('../../modules/contract/build/solc')
    }
}