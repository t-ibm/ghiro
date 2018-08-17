/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger

import com.softwareag.tom.is.pkg.dapp.Util
import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.Request
import com.softwareag.tom.protocol.jsonrpc.Response
import com.softwareag.tom.protocol.jsonrpc.ResponseMock
import com.softwareag.tom.protocol.jsonrpc.Service
import com.wm.app.b2b.server.PackageManager
import com.wm.app.b2b.server.ThreadManager
import com.wm.app.b2b.server.dispatcher.AbstractListener
import com.wm.app.b2b.server.dispatcher.exceptions.CommException
import com.wm.app.b2b.server.dispatcher.frameworks.DispatcherManager
import com.wm.app.b2b.server.dispatcher.frameworks.DispatcherManagerHelper
import com.wm.app.b2b.server.dispatcher.trigger.Trigger
import com.wm.app.b2b.server.dispatcher.trigger.control.ControlledTriggerSvcThreadPool
import com.wm.app.b2b.server.dispatcher.trigger.control.TriggerDispatcherStrategy
import com.wm.app.b2b.server.dispatcher.trigger.control.TriggerOutputControl
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message
import com.wm.app.b2b.server.dispatcher.wmmessaging.TriggerStatTracker
import com.wm.app.b2b.server.ns.Namespace
import com.wm.data.IData
import com.wm.data.IDataFactory
import com.wm.lang.ns.NSName
import com.wm.lang.ns.NSPackage
import com.wm.lang.ns.NSRecord
import com.wm.lang.ns.NSType
import com.wm.msg.ConditionFactory
import com.wm.msg.ICondition
import com.wm.msg.IMessage
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link DAppListener}.
 * @author tglaeser
 */
class DAppListenerTest extends ListenerSpecification {

    def "test log filter observable"() {
        given: 'the listener had some time to startup'
        sleep(1000)

        expect: 'the listener was successfully started'
        listener.running

        and: 'some messages were received'
        listener.queueSize > 0

        when: 'the listener will be stopped'
        listener.stop()

        then: 'the listener state changed accordingly'
        listener.stopped

        when: 'the received messages are now getting processed'
        processMessage()

        then: 'all messages were consumed'
        listener.queueSize == 0
    }
}

abstract class ListenerSpecification extends Specification {
    @Shared ControlledTriggerSvcThreadPool threadPool
    @Shared DAppListener listener
    @Shared String pdtName

    def setup() {
        // The test fixture
        pdtName = 'sample.SimpleStorage:LogAddress'
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
        IData messageTypeFilterPair = IDataFactory.create((Object[][]) [['messageType', pdt.getNSName().toString()], ['filter', 'contractAddress != null']])
        IData data = IDataFactory.create((Object[][]) [['messageTypeFilterPair', messageTypeFilterPair]])
        ICondition condition = ConditionFactory.getInstance(ConditionFactory.SIMPLE).create(data)
        ICondition[] conditions = [condition]
        TriggerStatTracker triggerStats = new TriggerStatTracker()
        Trigger trigger = Mock(Trigger)
        trigger.getTriggerStats() >> triggerStats
        trigger.setPackage(_ as NSPackage) >> { callRealMethod() }; trigger.getPackage() >> { callRealMethod() }; trigger.setPackage(pkg)
        trigger.setNSName(_ as NSName) >> { callRealMethod() }; trigger.getNSName() >> { callRealMethod() }; trigger.setNSName(pdt.getNSName())
        trigger.setConditions(_ as ICondition[]) >> { callRealMethod() }; trigger.getConditions() >> { callRealMethod() }; trigger.setConditions(conditions)
        trigger.getName() >> { callRealMethod() }
        trigger.findUMChannelFilterPairs() >> { callRealMethod() }
        trigger.processMessage(_ as IMessage) >> {
            // We stop calling the real method now as we are unable to obtain a reference to the invoke manager
        }
        ThreadManager.init()
        TriggerDispatcherStrategy triggerDispatcherStrategy = TriggerDispatcherStrategy.getInstance()
        TriggerOutputControl triggerOutputControl = new TriggerOutputControl(trigger, 1, DispatcherManager.create(DispatcherManagerHelper.CODE_PATH_DEFAULT_NAME))
        triggerDispatcherStrategy.register(triggerOutputControl)
        // Inject the thread pool size ... we need at least 2 thread to process the incoming messages
        threadPool = ControlledTriggerSvcThreadPool.getInstance()
        threadPool.max = 2
        threadPool.min = 0
        // Run the listener
        listener = new DAppListenerMock(trigger, threadPool)
        threadPool.runTarget(listener)
    }

    def processMessage() {
        while (listener.queueSize > 0) {
            println "queue size :: $listener.queueSize"
            Types.FilterLogType consumerEvent = listener.queue.poll()
            DAppExecutionTask task = new DAppExecutionTaskMock(consumerEvent, pdtName, listener)
            task.processMessage()
        }
    }
}

class DAppListenerMock extends DAppListener {
    DAppListenerMock(Trigger trigger, ControlledTriggerSvcThreadPool threadPool) {
        super(trigger, threadPool)
    }
    @Override protected void init() throws Exception {
        // Do not wait until IS is started
    }
    @Override protected void initMessageDispatcher() {
        // Do not initialize the dispatcher; instead we will bypass the dispatcher and process the message directly
    }
}

class DAppExecutionTaskMock extends DAppExecutionTask {
    DAppExecutionTaskMock(Types.FilterLogType consumerEvent, String pdtName, AbstractListener<Types.FilterLogType> listener) throws CommException {
        super(consumerEvent, pdtName, listener)
    }
    @Override protected void setup(Message<Types.FilterLogType> message) {
        // Do not setup an IS context
    }
}