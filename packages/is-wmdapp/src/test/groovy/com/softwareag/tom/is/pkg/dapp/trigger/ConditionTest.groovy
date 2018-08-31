/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger

import com.wm.lang.ns.NSName
import com.wm.msg.ICondition
import spock.lang.Specification

/**
 * System under specification: {@link Condition}.
 * @author tglaeser
 */
class ConditionTest extends Specification {

    def "test simple condition"() {
        given: 'a simple condition definition'
        String pdtName = 'sample.SimpleStorage:LogAddress'
        String serviceName = 'pub.flow:debugLog'
        String filter = 'contractAddress != null'
        ICondition condition = Condition.create(pdtName, serviceName, filter).asCondition()

        expect: 'to find the object reflecting the definition'
        condition.type == 'simple'
        condition.name == "Condition $pdtName"
        condition.serviceName == NSName.create(serviceName)
        condition.subscriptionEntries[0] == pdtName
        condition.subscriptionEntryFilterPairs[0].messageType == pdtName
        condition.subscriptionEntryFilterPairs[0].filter.source == filter
    }
}
