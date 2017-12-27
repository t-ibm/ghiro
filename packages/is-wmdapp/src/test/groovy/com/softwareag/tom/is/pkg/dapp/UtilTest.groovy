/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp

import com.wm.lang.ns.NSName
import com.wm.lang.ns.NSSignature
import spock.lang.Specification

/**
 * System under specification: {@link Util}.
 * @author tglaeser
 */
class UtilTest extends Specification {

    def "test contract to ns node conversion"() {
        given: 'the contracts can be retrieved from the contract registry'
        Map<NSName, NSSignature> functions = Util.create().getFunctions()

        expect: 'to retrieve a populated map of ns nodes'
        functions.size() == 10

        when: 'a particular ns node is retrieved'
        NSName nsName = NSName.create('sample.util.Console:uintToBytes')
        NSSignature nsSignature = functions[nsName]

        then: 'the signature of this ns node is as expected'
        nsName.fullName == 'sample.util.Console:uintToBytes'
        nsName.interfaceName as String == 'sample.util.Console'
        nsName.nodeName as String == 'uintToBytes'
        nsSignature.input.fields.length == 1
        nsSignature.input.fields[0].name == 'v'
        nsSignature.output.fields.length == 1
        nsSignature.output.fields[0].name == 'ret'

        when: 'a particular ns node is retrieved'
        nsSignature = functions[NSName.create('sample.util.Console:log')]
        nsSignature.input = nsSignature.getInput()

        then: 'the signature of this ns node is as expected'
        nsSignature.input.fields.length == 1
        nsSignature.input.fields[0].name == 'x'
        nsSignature.output.fields.length == 0
    }
}