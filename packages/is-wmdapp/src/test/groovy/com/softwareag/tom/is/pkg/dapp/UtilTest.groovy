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
        Map<NSName, NSSignature> contracts = Util.create().getFunctions()

        expect: 'to retrieve a populated list of ns nodes'
        contracts.size() == 7
    }
}