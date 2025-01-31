package com.softwareag.tom.env

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class NodeSpecification extends Specification {

    @com.softwareag.tom.extension.Node ConfigObject defaultConfig

    @Shared String[][] configs = [
            ['one', 'two', 'three', 'forty-two'],
            ['ghirouno', 'ghirodue', 'ghirotre', 'default'],
    ]

    def "test default configuration"() {
        expect: 'the default configuration'
        defaultConfig.node.name == 'default'
        defaultConfig.node.host.ip == '127.0.0.1'
        defaultConfig.node.host.grpc.port == 10997
        defaultConfig.node.host.info.port == 26658
        defaultConfig.node.host.web3.port == 26660
    }

    @Unroll def "test node '#node' configuration"(String node, String name) {
        when: 'a new config object is created'
        ConfigObject config = new ConfigSlurper(node).parse(Node.class)

        then: 'the config objects name has the expected value'
        config.node.name == name

        where: 'the node property has any arbitary value'
        node << configs[0]
        name << configs[1]
    }
}