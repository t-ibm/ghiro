/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.pkg

import com.softwareag.tom.is.pkg.dapp.Util
import com.softwareag.tom.is.pkg.dapp.trigger.DAppListener
import com.wm.app.b2b.server.PackageManager
import com.wm.app.b2b.server.ThreadManager
import com.wm.app.b2b.server.TriggerFactory
import com.wm.app.b2b.server.dispatcher.frameworks.DispatcherManager
import com.wm.app.b2b.server.dispatcher.frameworks.DispatcherManagerHelper
import com.wm.app.b2b.server.dispatcher.trigger.Trigger
import com.wm.app.b2b.server.dispatcher.trigger.control.ControlledTriggerSvcThreadPool
import com.wm.app.b2b.server.dispatcher.trigger.control.TriggerDispatcherStrategy
import com.wm.app.b2b.server.dispatcher.trigger.control.TriggerOutputControl
import com.wm.app.b2b.server.ns.Namespace
import com.wm.data.IData
import com.wm.data.IDataFactory
import com.wm.lang.ns.NSName
import com.wm.lang.ns.NSPackage
import com.wm.lang.ns.NSRecord
import com.wm.lang.ns.NSType
import com.wm.msg.ConditionFactory
import com.wm.msg.ICondition
import spock.lang.Specification

/**
 * System under specification: {@link Util}.
 * @author tglaeser
 */
class UtilTest extends Specification {


    def "test create solidity contract and listen to events"() {
        given: 'a valid Solidity contract'
        NSName logFuntion = NSName.create('sample.SimpleStorage:log')
        // The test fixture
        String pdtName = 'sample.SimpleStorage:LogAddress'
        // Create publishable document type
        NSPackage pkg = PackageManager.getPackage("WmDAppContract")
        IData eventDescription = IDataFactory.create((Object[][])[['brokerEventTypeName', 'MyBrokerEventTypeName'], ['timeToLive', 0]])
        IData record = IDataFactory.create((Object[][])[['node_nsName', pdtName], ['node_type', NSType.create('record')], ['eventDescription', eventDescription]])
        NSRecord pdt = NSRecord.createRecord(Namespace.current(), record)
        pdt.setPackage(pkg)
        assert pdt.isPublishable()
        Namespace.current().putNode(pdt)
        // Create trigger
        IData messageTypeFilterPair = IDataFactory.create((Object[][])[['messageType', pdt.getNSName().toString()], ['filter', 'MyFilter'], ['umFilter', 'MyUmFilter']])
        IData data = IDataFactory.create((Object[][])[['messageTypeFilterPair', messageTypeFilterPair]])
        ICondition condition = ConditionFactory.getInstance(ConditionFactory.SIMPLE).create(data)
        ICondition[] conditions = [condition]
        Trigger trigger = TriggerFactory.createTrigger(pkg, NSName.create(pdtName), conditions)
        ThreadManager.init()
        TriggerDispatcherStrategy triggerDispatcherStrategy = TriggerDispatcherStrategy.getInstance()
        TriggerOutputControl triggerOutputControl = new TriggerOutputControl(trigger, 1, DispatcherManager.create(DispatcherManagerHelper.CODE_PATH_DEFAULT_NAME))
        triggerDispatcherStrategy.register(triggerOutputControl)

        when: println '(1) The contract gets deployed'
        String contractAddress = Util.instance.deployContract(pdt.getNSName())

        then: 'a valid response is received'
        contractAddress.size() == 42

        when: println '(2) we register for events with the new contract account'
        // Run the listener
        ControlledTriggerSvcThreadPool threadPool = ControlledTriggerSvcThreadPool.getInstance()
        DAppListener listener = new DAppListenerMock(trigger, threadPool)
        threadPool.runTarget(listener)
        sleep(1000)

        then: 'the ReactiveX system gets properly initialized'
        listener.running
        listener.getQueueSize() == 0

        when: println '(3) function "log" is executed 3 times'
        3.times {
            Util.instance.call(logFuntion, IDataFactory.create())
        }
        sleep(10000)

        then: 'a valid response is received'
        listener.getQueueSize() == 2 //TODO :: Seems we are missing the first event

        when: println '(4) the subscription is terminated'
        listener.stop()

        then: 'the subscriber has been removed'
        listener.stopped
    }
}

class DAppListenerMock extends DAppListener {
    DAppListenerMock(Trigger trigger, ControlledTriggerSvcThreadPool threadPool) {
        super(trigger, threadPool)
    }

    @Override protected void init() throws Exception {
        // Do not wait until IS is started
    }
}