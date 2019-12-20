/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package wm.dapp

import com.softwareag.tom.is.pkg.dapp.ServiceSupplierWeb3
import com.softwareag.tom.is.pkg.dapp.Util
import com.wm.app.b2b.server.InvokeState
import com.wm.app.b2b.server.ServiceException
import com.wm.data.IData
import com.wm.data.IDataFactory
import com.wm.lang.flow.FlowElement
import com.wm.lang.flow.FlowState
import com.wm.lang.ns.NSName
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link Contract}.
 * @author tglaeser
 */
class ContractSpecification extends Specification{

    @Shared Contract contract = new Contract()
    @Shared IData pipeline
    @Shared IData expected

    /**
     * Mocking the underlying IS implementation.
     */
    def setupSpec() {
        FlowElement flowRoot = Mock(FlowElement); flowRoot.getNSName() >> { NSName.create('sample.util.Console:log') }
        FlowElement flowElement = Mock(FlowElement); flowElement.getFlowRoot() >> { flowRoot }
        FlowState flowState = Mock(FlowState); flowState.current() >> { flowElement }
        InvokeState invokeState = GroovySpy(InvokeState, global:false) as InvokeState; invokeState.getFlowState() >> { flowState }
    }

    /**
     * The test fixture.
     */
    def setup() {
        pipeline = IDataFactory.create()
        expected = pipeline
    }

    def 'test positive call'() {
        given: 'the underlying implementation methods are mocked'
        Contract.util = Mock(Util)
        Contract.util.web3() >> Mock(ServiceSupplierWeb3)

        when: 'the service is invoked'
        contract.call(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        notThrown ServiceException
    }

    def 'test negative call'() {
        given: 'the underlying implementation methods are mocked'
        Contract.util = Mock(Util)
        Contract.util.web3() >> Mock(ServiceSupplierWeb3)
        Contract.util.web3().runContract(_ as NSName, _ as IData, false) >> {
            throw new IOException()
        }

        when: 'the service is invoked'
        contract.call(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        thrown ServiceException
    }

    def 'test positive send transaction'() {
        given: 'the underlying implementation methods are mocked'
        Contract.util = Mock(Util)
        Contract.util.web3() >> Mock(ServiceSupplierWeb3)

        when: 'the service is invoked'
        contract.sendTransaction(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        notThrown ServiceException
    }

    def 'test negative send transaction'() {
        given: 'the underlying implementation methods are mocked'
        Contract.util = Mock(Util)
        Contract.util.web3() >> Mock(ServiceSupplierWeb3)
        Contract.util.web3().runContract(_ as NSName, _ as IData, true) >> {
            throw new IOException()
        }

        when: 'the service is invoked'
        contract.sendTransaction(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        thrown ServiceException
    }
}