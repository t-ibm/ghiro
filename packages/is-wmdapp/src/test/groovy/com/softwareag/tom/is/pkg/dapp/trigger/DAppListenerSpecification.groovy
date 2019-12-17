/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger

import com.softwareag.tom.is.pkg.dapp.RuntimeBaseSpecification
import com.softwareag.tom.is.pkg.dapp.ServiceSupplierBurrow
import com.softwareag.tom.is.pkg.dapp.ServiceSupplierWeb3
import com.softwareag.tom.is.pkg.dapp.Util
import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.abi.Types
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
import io.grpc.stub.StreamObserver
import org.hyperledger.burrow.rpc.RpcEvents
import rx.Observer
import rx.Subscription
import spock.lang.Shared
import spock.lang.Unroll

/**
 * System under specification: {@link DAppListener}.
 * @author tglaeser
 */
class DAppListenerSpecification extends ListenerBaseSpecification {

    def "test negative start listener"() {
        when: 'the listener is not properly configured'
        this.listener = new DAppListener(this.trigger, this.threadPool)
        this.threadPool.runTarget(this.listener)

        then: 'the listener failed to start'
        !this.listener.running
        this.listener.queueSize == 0

        and: 'no exception has been thrown'
        notThrown(Exception)
    }

    @Unroll def "test log filter observable #dappListener.class.name"() {
        given: 'the listener had some time to startup'
        this.listener = dappListener
        this.threadPool.runTarget(this.listener)
        sleep(1000)

        expect: 'the listener was successfully started'
        this.listener.running

        and: 'some messages were received'
        this.listener.queueSize > 0

        when: 'the listener will be stopped'
        this.listener.stop()

        then: 'the listener state changed accordingly'
        this.listener.stopped

        when: 'the received messages are now getting processed'
        this.processMessage()

        then: 'all messages were consumed'
        this.listener.queueSize == 0

        where:
        dappListener << [
            new DAppListenerWeb3(this.trigger, this.threadPool),
            new DAppListenerBurrow(this.trigger, this.threadPool),
        ]
    }
}

/**
 * A base specification providing support for common IS trigger handling.
 */
abstract class ListenerBaseSpecification extends RuntimeBaseSpecification {

    @Shared String pdtName
    @Shared Trigger trigger
    @Shared AbstractListener listener
    @Shared ControlledTriggerSvcThreadPool threadPool

    @Shared Util util = Util.instance()

    @Override def setupSpec() {
        setupTrigger()
        util.web3 = new ServiceSupplierWeb3(util, Web3Service.build(web3))
        util.burrow = new ServiceSupplierBurrow(util, burrowQuery, burrowTransact, burrowEvents)
        // Inject the thread pool size ... we need at least 2 thread to process the incoming messages
        threadPool = ControlledTriggerSvcThreadPool.getInstance()
        threadPool.max = 2
        threadPool.min = 0
    }

    def setupTrigger() {
        // Test fixture
        pdtName = 'sample.SimpleStorage:LogAddressDoc'
        String triggerName = 'sample.SimpleStorage:LogAddressTrg'
        String serviceName = 'pub.flow:debugLog'
        // Create publishable document type
        NSName pdtNsName = NSName.create(pdtName)
        NSRecord pdt = util.getPublishableDocumentType(pdtNsName)
        assert pdt.isPublishable()
        Namespace.current().putNode(pdt)
        // Create service to invoke
        NSName svcNsName = NSName.create(serviceName)
        FlowSvcImpl svc = Util.getResponseService(svcNsName)
        Namespace.current().putNode(svc)
        // Create trigger
        NodeMaster.registerFactory(NSTrigger.TYPE.getValue(), new TriggerFactory())
        trigger = Util.createTrigger(NSName.create(triggerName))
        Util.addCondition(trigger, Condition.create(pdtNsName, svcNsName,'contractAddress != null').asCondition())
        // Inject mock invoke manager into Trigger
        trigger.invokeManager = Mock(InvokeManager)
        // Initialize trigger manager
        ThreadManager.init()
        TriggerDispatcherStrategy triggerDispatcherStrategy = TriggerDispatcherStrategy.getInstance()
        TriggerOutputControl triggerOutputControl = new TriggerOutputControl(trigger, 1, DispatcherManager.create(DispatcherManagerHelper.CODE_PATH_DEFAULT_NAME))
        triggerDispatcherStrategy.register(triggerOutputControl)
        // Remember the contract address; implies the contract was deployed
        util.storeContractAddress(pdt.getNSName(), responseMock.contractAddress)
    }

    def <E> void processMessage() {
        while (listener.queueSize > 0) {
            println "queue size :: $listener.queueSize"
            E consumerEvent = listener.queue.poll()
            DAppExecutionTask<E> task = new DAppExecutionTaskMock<>(consumerEvent, pdtName, listener)
            task.processMessage()
        }
    }
}

class DAppListenerWeb3 extends DAppListenerBase<Types.FilterLogType> {

    private Subscription subscription

    DAppListenerWeb3(Trigger trigger, ControlledTriggerSvcThreadPool threadPool) {
        super(trigger, threadPool)
    }

    @Override void subscribe() throws IOException {
        Observer<Types.FilterLogType> observer = [
            onCompleted: {
                stopProcessing()
            },
            onError    : { Throwable e ->
                throw e
            },
            onNext     : { Types.FilterLogType result ->
                _messageQueue.put(result)
            }
        ] as Observer<Types.FilterLogType>
        subscription = Util.instance().web3().subscribe(_trigger.getNSName(), observer)
    }

    @Override void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe()
        }
    }

    @Override protected void init() throws Exception {
        // Do not wait until IS is started
    }
    @Override protected void initMessageDispatcher() {
        // Do not initialize the dispatcher; instead we will bypass the dispatcher and process the message directly
    }
}

class DAppListenerBurrow extends DAppListenerBase<RpcEvents.EventsResponse> {

    private com.softwareag.tom.protocol.grpc.stream.Subscription subscription

    DAppListenerBurrow(Trigger trigger, ControlledTriggerSvcThreadPool threadPool) {
        super(trigger, threadPool)
    }

    @Override void subscribe() throws IOException {
        StreamObserver<RpcEvents.EventsResponse> observer = [
            onCompleted: {
                stopProcessing()
            },
            onError    : { Throwable e ->
                throw e
            },
            onNext     : { RpcEvents.EventsResponse result ->
                _messageQueue.put(result)
            }
        ] as StreamObserver<RpcEvents.EventsResponse>
        subscription = Util.instance().burrow().subscribe(_trigger.getNSName(), observer)
    }

    @Override void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe()
        }
    }

    @Override protected void init() throws Exception {
        // Do not wait until IS is started
    }
    @Override protected void initMessageDispatcher() {
        // Do not initialize the dispatcher; instead we will bypass the dispatcher and process the message directly
    }
}

class DAppExecutionTaskMock<E> extends DAppExecutionTask<E> {
    DAppExecutionTaskMock(E consumerEvent, String pdtName, AbstractListener<E> listener) throws CommException {
        super(consumerEvent, pdtName, listener)
    }
    @Override protected void setup(Message<E> message) {
        // Do not setup an IS context
    }
}