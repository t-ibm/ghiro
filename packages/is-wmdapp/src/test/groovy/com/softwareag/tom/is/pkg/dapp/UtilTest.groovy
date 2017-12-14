/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp

import com.wm.lang.ns.NSName
import spock.lang.Specification

/**
 * System under specification: {@link Util}.
 * @author tglaeser
 */
class UtilTest extends Specification {

    def "test default configuration"() {
        given: 'a parameter of type "bool"'
        List<NSName> contracts = Util.getContracts()

        expect: 'the default configuration'
        contracts.size() == 9
    }
}