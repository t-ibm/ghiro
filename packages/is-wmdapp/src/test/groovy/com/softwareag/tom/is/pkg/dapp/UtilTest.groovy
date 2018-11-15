/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp

import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.Request
import com.softwareag.tom.protocol.jsonrpc.Response
import com.softwareag.tom.protocol.jsonrpc.Service
import com.softwareag.tom.protocol.jsonrpc.ResponseMock
import com.softwareag.tom.protocol.util.HexValue
import com.softwareag.util.IDataMap
import com.wm.app.b2b.server.FlowSvcImpl
import com.wm.app.b2b.server.NodeMaster
import com.wm.app.b2b.server.TriggerFactory
import com.wm.app.b2b.server.dispatcher.trigger.Trigger
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message
import com.wm.data.IData
import com.wm.data.IDataFactory
import com.wm.lang.ns.NSName
import com.wm.lang.ns.NSRecord
import com.wm.lang.ns.NSSignature
import com.wm.lang.ns.NSTrigger
import rx.Observable

import static Util.SUFFIX_REQ
import static Util.SUFFIX_DOC
import static Util.SUFFIX_REP

/**
 * System under specification: {@link Util}.
 * @author tglaeser
 */
class UtilTest extends RuntimeSpecification {

    def "test contract function to ns node conversion"() {
        given: 'the contracts can be retrieved from the contract registry'
        Map<String, FlowSvcImpl> functions = Util.instance.getFunctions(false)

        expect: 'to retrieve a populated map of ns nodes'
        functions.size() == 6

        when: 'a particular ns node is retrieved'
        String functionName = 'sample.util.Console:uintToBytes'
        NSName nsName = NSName.create("$functionName$SUFFIX_REQ")
        NSSignature nsSignature = functions[functionName].signature

        then: 'the signature of this ns node is as expected'
        nsName.fullName == "sample.util.Console:uintToBytes$SUFFIX_REQ"
        nsName.interfaceName as String == 'sample.util.Console'
        nsName.nodeName as String == "uintToBytes$SUFFIX_REQ"
        nsSignature.input.fields.length == 1
        nsSignature.input.fields[0].name == 'v'
        nsSignature.output.fields.length == 1
        nsSignature.output.fields[0].name == 'ret'

        when: 'a particular ns node is retrieved'
        functionName = 'sample.util.Console:log'
        nsSignature = functions[functionName].signature
        nsSignature.input = nsSignature.getInput()

        then: 'the signature of this ns node is as expected'
        nsSignature.input.fields.length == 1
        nsSignature.input.fields[0].name == 'x'
        nsSignature.output.fields.length == 0
    }

    def "test contract event to ns node conversion"() {
        given: 'the contracts can be retrieved from the contract registry'
        NodeMaster.registerFactory(NSTrigger.TYPE.getValue(), new TriggerFactory())
        Map<String,Event> events = Util.instance.getEvents(false)

        expect: 'to retrieve a populated map of ns nodes'
        events.size() == 4

        when: 'a particular entry is retrieved'
        Event event = events.get('sample.util.Console:LogAddress')

        then: 'its value is non null'
        event != null

        when: 'the entry value is retrieved'
        NSRecord nsRecord = event.pdt
        Trigger trigger = event.trigger
        FlowSvcImpl service = event.service

        then: 'the document type of this ns node is as expected'
        nsRecord.getNSName() == NSName.create("sample.util.Console:LogAddress$SUFFIX_DOC")
        nsRecord.fields.length == 2
        nsRecord.fields[0].name == '_env'
        nsRecord.fields[1].name == 'contractAddress'
        nsRecord.isPublishable()
        trigger.getNSName() == NSName.create("sample.util.Console:trigger")
        service.getNSName() == NSName.create("sample.util.Console:LogAddress$SUFFIX_REP")

        when: 'a particular entry is retrieved'
        event = events.get('sample.util.Console:LogUint')

        then: 'its value is non null'
        event != null

        when: 'the entry value is retrieved'
        nsRecord = event.pdt
        trigger = event.trigger
        service = event.service

        then: 'the document type of this ns node is as expected'
        nsRecord.getNSName() == NSName.create("sample.util.Console:LogUint$SUFFIX_DOC")
        nsRecord.fields.length == 2
        nsRecord.fields[0].name == '_env'
        nsRecord.fields[1].name == 'ret'
        nsRecord.isPublishable()
        trigger.getNSName() == NSName.create("sample.util.Console:trigger")
        service.getNSName() == NSName.create("sample.util.Console:LogUint$SUFFIX_REP")
    }

    def "test contract address mapping"() {
        given: 'the contracts can be retrieved from the contract registry'
        IData[] contractAddresses = Util.instance.getContractAddresses()

        expect: 'to retrieve a populated list of contract address mappings'
        contractAddresses.length == 2

        when: 'a particular item is retrieved from the list'
        IDataMap contract = new IDataMap(contractAddresses[0])

        then: 'the values are as expected'
        contract.get('uri') == 'sample/util/Console'
        contract.get('address') == null
    }

    def "test call log"() {
        given: 'the needed instances and handling of service layer communications'
        NSName nsName = NSName.create('sample.SimpleStorage:log')
        ResponseMock responseMock = new ResponseMock()
        Service service = Mock(Service)
        service.send(_ as Request, _ as Class) >> { Request request, Class c ->
            println ">>> $request"
            Response response = responseMock.getResponse(request)
            println "<<< $response"
            response
        }
        Util.instance.web3Service = Web3Service.build(service)

        when: 'the contract address is remembered; implying the contract was deployed'
        if (!Util.instance.isContractDeployed(nsName)) {
            Util.instance.storeContractAddress(nsName, responseMock.contractAddress)
        }

        and: 'we attempt to get the log observable'
        IData pipeline = IDataFactory.create()
        Util.instance.call(nsName, pipeline)

        then: 'a valid instance is retrieved'
        pipeline == IDataFactory.create()
    }

    def "test log event"() {
        given: 'the needed instances and handling of service layer communications'
        NSName nsName = NSName.create('sample.SimpleStorage:LogAddress')
        ResponseMock responseMock = new ResponseMock()
        Service service = Mock(Service)
        service.send(_ as Request, _ as Class) >> { Request request, Class c ->
            println ">>> $request"
            Response response = responseMock.getResponse(request)
            println "<<< $response"
            response
        }
        Util.instance.web3Service = Web3Service.build(service)

        when: 'the contract address is remembered; implying the contract was deployed'
        if (!Util.instance.isContractDeployed(nsName)) {
            Util.instance.storeContractAddress(nsName, responseMock.contractAddress)
        }

        and: 'we attempt to get the log observable'
        Observable<Types.FilterLogType> logObservable = Util.instance.getLogObservable(nsName)

        then: 'a valid instance is retrieved'
        logObservable != null

        when: 'log events are received'
        List<Types.FilterLogType> filterChanges = responseMock.getExpectedFilterChanges()

        and: 'the first log event is being decoded'
        Message<Types.FilterLogType> msg = Util.instance.decodeLogEvent(nsName, filterChanges[0])

        then: 'the resulting pipeline has the expected values'
        msg.getIData() == IDataFactory.create((Object[][])[
            ['_env', IDataFactory.create((Object[][])[['uuid', '']])],
            ['contractAddress', HexValue.toBigInteger('1')]
        ])
    }
}