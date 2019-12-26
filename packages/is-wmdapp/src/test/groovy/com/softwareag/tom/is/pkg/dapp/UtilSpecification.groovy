/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp

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
import io.grpc.stub.StreamObserver
import rx.Observer
import spock.lang.Unroll

import static Util.SUFFIX_REQ
import static Util.SUFFIX_DOC
import static Util.SUFFIX_REP

/**
 * System under specification: {@link Util}.
 * @author tglaeser
 */
class UtilSpecification extends RuntimeBaseSpecification {

    def "test contract function to ns node conversion"() {
        given: 'the contracts can be retrieved from the contract registry'
        Map<String, FlowSvcImpl> functions = util.getFunctions(false)

        expect: 'to retrieve a populated map of ns nodes'
        functions.size() == 40

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
        Map<String,Event> events = util.getEvents(false)

        expect: 'to retrieve a populated map of ns nodes'
        events.size() == 12

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
        IData[] contractAddresses = util.getContractAddresses()

        expect: 'to retrieve a populated list of contract address mappings'
        contractAddresses.length == 7

        when: 'a particular item is retrieved from the list'
        IDataMap contract = new IDataMap(contractAddresses[1])

        then: 'the values are as expected'
        contract.get('uri') == 'sample/util/Console'
        contract.get('address') == null
    }

    @Unroll def "test call log #serviceSupplier.class.name"(ServiceSupplierBase serviceSupplier) {
        given: 'the needed instances and handling of service layer communications'
        NSName nsName = NSName.create('sample.SimpleStorage:log')

        when: 'the contract address is remembered; implying the contract was deployed'
        if (!util.isContractDeployed(nsName)) {
            util.storeContractAddress(nsName, responseMock.contractAddress)
        }

        and: 'we attempt to get the log observable'
        IData pipeline = IDataFactory.create()
        serviceSupplier.call(nsName, pipeline)

        then: 'a valid instance is retrieved'
        pipeline == IDataFactory.create()

        where: 'the service provider is from the list of supported providers'
        serviceSupplier << [
            util.web3(),
            util.burrow()
        ]
    }

    @Unroll def 'test log event #serviceSupplier.class.name'(ServiceSupplierBase serviceSupplier, Object observer, List filterChanges) {
        given: 'the needed instances and handling of service layer communications'
        NSName nsName = NSName.create('sample.SimpleStorage:LogAddress')

        when: 'the contract address is remembered; implying the contract was deployed'
        if (!util.isContractDeployed(nsName)) {
            util.storeContractAddress(nsName, responseMock.contractAddress)
        }

        and: 'we attempt to get the log observable'
        serviceSupplier.subscribe(nsName, observer)

        and: 'the first log event is being decoded'
        Message msg = serviceSupplier.decodeLogEvent(nsName, filterChanges[0])

        then: 'the resulting pipeline has the expected values'
        msg.getIData() == IDataFactory.create((Object[][])[
            ['_env', IDataFactory.create((Object[][])[['uuid', '30']])],
            ['contractAddress', HexValue.toBigInteger('33F71BB66F8994DD099C0E360007D4DEAE11BFFE')]
        ])

        where: 'the service provider is from the list of supported providers'
        serviceSupplier | observer                | filterChanges
        util.web3()     | Mock(Observer)          | responseMock.getExpectedFilterChangesWeb3()
        util.burrow()   | Mock(StreamObserver)    | responseMock.getExpectedFilterChangesBurrow()
    }
}