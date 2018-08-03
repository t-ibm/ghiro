/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp

import com.softwareag.tom.is.pkg.dapp.trigger.DAppListener
import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.jsonrpc.Request
import com.softwareag.tom.protocol.jsonrpc.Response
import com.softwareag.tom.protocol.jsonrpc.ResponseMock
import com.softwareag.tom.protocol.jsonrpc.Service
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
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link DAppListener}.
 * @author tglaeser
 */
class DAppListenerTest extends ListenerSpecification {

    def "test log filter observable"() {
        given: 'the listener some time to startup'
        sleep(10000)

        expect: 'the listener was successfully started'
        listener.running
        listener.queueSize > 0

        when: 'the listener will be stopped'
        listener.stop()

        then: 'the listener state changed accordingly'
        listener.stopped
    }
}

abstract class ListenerSpecification extends Specification {
    @Shared Trigger trigger
    @Shared DAppListener listener

    def setup() {
        // The test fixture
        String pdtName = 'sample.SimpleStorage:LogAddress'
        // Create publishable document type
        NSPackage pkg = PackageManager.getPackage("WmDAppContract")
        IData eventDescription = IDataFactory.create((Object[][]) [['brokerEventTypeName', 'MyBrokerEventTypeName'], ['timeToLive', 0]])
        IData record = IDataFactory.create((Object[][]) [['node_nsName', pdtName], ['node_type', NSType.create('record')], ['eventDescription', eventDescription]])
        NSRecord pdt = NSRecord.createRecord(Namespace.current(), record)
        pdt.setPackage(pkg)
        assert pdt.isPublishable()
        Namespace.current().putNode(pdt)
        // Create needed instances
        ResponseMock filterResponseMock = new ResponseMock()
        Service service = Mock(Service)
        // Handle service layer communications
        service.send(_ as Request, _ as Class) >> { Request request, Class c ->
            println ">>> $request"
            Response response = filterResponseMock.getResponse(request)
            println "<<< $response"
            response
        }
        // Inject mock service into Util class
        Util.instance.web3Service = Web3Service.build(service)
        // Remember the contract address; implies the contract was deployed
        Util.instance.storeContractAddress(pdt.getNSName(), filterResponseMock.contractAddress)
        // Create trigger
        IData messageTypeFilterPair = IDataFactory.create((Object[][]) [['messageType', pdt.getNSName().toString()], ['filter', 'MyFilter'], ['umFilter', 'MyUmFilter']])
        IData data = IDataFactory.create((Object[][]) [['messageTypeFilterPair', messageTypeFilterPair]])
        ICondition condition = ConditionFactory.getInstance(ConditionFactory.SIMPLE).create(data)
        ICondition[] conditions = [condition]
        trigger = TriggerFactory.createTrigger(pkg, NSName.create(pdtName), conditions)
        ThreadManager.init()
        TriggerDispatcherStrategy triggerDispatcherStrategy = TriggerDispatcherStrategy.getInstance()
        TriggerOutputControl triggerOutputControl = new TriggerOutputControl(trigger, 1, DispatcherManager.create(DispatcherManagerHelper.CODE_PATH_DEFAULT_NAME))
        triggerDispatcherStrategy.register(triggerOutputControl)
        // Run the listener
        ControlledTriggerSvcThreadPool threadPool = ControlledTriggerSvcThreadPool.getInstance()
        listener = new DAppListenerMock(trigger, threadPool)
        threadPool.runTarget(listener)
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