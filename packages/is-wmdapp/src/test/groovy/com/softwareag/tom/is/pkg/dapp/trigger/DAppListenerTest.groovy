/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger

import com.softwareag.tom.is.pkg.dapp.RuntimeSpecification
import com.softwareag.tom.is.pkg.dapp.Util
import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.Request
import com.softwareag.tom.protocol.jsonrpc.Response
import com.softwareag.tom.protocol.jsonrpc.ResponseMock
import com.softwareag.tom.protocol.jsonrpc.Service
import com.wm.app.b2b.server.FlowSvcImpl
import com.wm.app.b2b.server.NodeMaster
import com.wm.app.b2b.server.ThreadManager
import com.wm.app.b2b.server.TriggerFactory
import com.wm.app.b2b.server.dispatcher.AbstractListener
import com.wm.app.b2b.server.dispatcher.exceptions.CommException
import com.wm.app.b2b.server.dispatcher.frameworks.DispatcherManager
import com.wm.app.b2b.server.dispatcher.frameworks.DispatcherManagerHelper
import com.wm.app.b2b.server.dispatcher.trigger.Trigger
import com.wm.app.b2b.server.dispatcher.trigger.control.ControlledTriggerSvcThreadPool
import com.wm.app.b2b.server.dispatcher.trigger.control.TriggerDispatcherStrategy
import com.wm.app.b2b.server.dispatcher.trigger.control.TriggerOutputControl
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message
import com.wm.app.b2b.server.invoke.InvokeManager
import com.wm.app.b2b.server.ns.Namespace
import com.wm.lang.ns.NSName
import com.wm.lang.ns.NSRecord
import com.wm.lang.ns.NSTrigger
import spock.lang.Shared

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

abstract class ListenerSpecification extends RuntimeSpecification {
    @Shared ControlledTriggerSvcThreadPool threadPool
    @Shared DAppListener listener
    @Shared String pdtName

    def setup() {
        // Test fixture
        pdtName = 'sample.SimpleStorage:LogAddressDoc'
        String triggerName = 'sample.SimpleStorage:LogAddressTrg'
        String serviceName = 'pub.flow:debugLog'
        // Create publishable document type
        NSName pdtNsName = NSName.create(pdtName)
        NSRecord pdt = Util.instance.getPublishableDocumentType(pdtNsName)
        assert pdt.isPublishable()
        Namespace.current().putNode(pdt)
        // Create service to invoke
        NSName svcNsName = NSName.create(serviceName)
        FlowSvcImpl svc = Util.instance.getResponseService(svcNsName)
        Namespace.current().putNode(svc)
        // Create trigger
        NodeMaster.registerFactory(NSTrigger.TYPE.getValue(), new TriggerFactory())
        Trigger trigger = Util.instance.createTrigger(NSName.create(triggerName))
        Util.instance.addCondition(trigger, Condition.create(pdtNsName, svcNsName,'contractAddress != null').asCondition())
        // Inject mock invoke manager into Trigger
        trigger.invokeManager = Mock(InvokeManager)
        // Initialize trigger manager
        ThreadManager.init()
        TriggerDispatcherStrategy triggerDispatcherStrategy = TriggerDispatcherStrategy.getInstance()
        TriggerOutputControl triggerOutputControl = new TriggerOutputControl(trigger, 1, DispatcherManager.create(DispatcherManagerHelper.CODE_PATH_DEFAULT_NAME))
        triggerDispatcherStrategy.register(triggerOutputControl)
        // Inject the thread pool size ... we need at least 2 thread to process the incoming messages
        threadPool = ControlledTriggerSvcThreadPool.getInstance()
        threadPool.max = 2
        threadPool.min = 0
        // Create needed instances
        ResponseMock filterResponseMock = new ResponseMock()
        Service jsonRpcService = Mock(Service)
        // Handle service layer communications
        jsonRpcService.send(_ as Request, _ as Class) >> { Request request, Class c ->
            println ">>> $request"
            Response response = filterResponseMock.getResponse(request)
            println "<<< $response"
            response
        }
        // Inject mock service into Util
        Util.instance.web3Service = Web3Service.build(jsonRpcService)
        // Remember the contract address; implies the contract was deployed
        Util.instance.storeContractAddress(pdt.getNSName(), filterResponseMock.contractAddress)
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