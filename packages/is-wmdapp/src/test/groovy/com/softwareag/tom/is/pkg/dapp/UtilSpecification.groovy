/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp

import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.api.BurrowEvents
import com.softwareag.tom.protocol.api.BurrowQuery
import com.softwareag.tom.protocol.api.BurrowTransact
import com.softwareag.tom.protocol.jsonrpc.Request
import com.softwareag.tom.protocol.jsonrpc.Response
import com.softwareag.tom.protocol.jsonrpc.ResponseMock
import com.softwareag.tom.protocol.jsonrpc.Service
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
import org.hyperledger.burrow.Acm
import org.hyperledger.burrow.execution.Exec
import org.hyperledger.burrow.rpc.RpcEvents
import org.hyperledger.burrow.rpc.RpcQuery
import org.hyperledger.burrow.txs.Payload
import rx.Observer
import spock.lang.Shared
import spock.lang.Unroll

import java.util.concurrent.Executors

import static Util.SUFFIX_REQ
import static Util.SUFFIX_DOC
import static Util.SUFFIX_REP

/**
 * System under specification: {@link Util}.
 * @author tglaeser
 */
class UtilSpecification extends RuntimeBaseSpecification {

    @Shared Service web3 = Mock(Service)
    @Shared ResponseMock responseMock = new ResponseMock()
    @Shared BurrowQuery burrowQuery
    @Shared BurrowTransact burrowTransact
    @Shared BurrowEvents burrowEvents

    @Override def setupSpec() {
        // JSON-RPC
        responseMock = new ResponseMock()
        web3 = Mock(Service)
        web3.send(_ as Request, _ as Class) >> { Request request, Class c ->
            println ">>> $request"
            Response response = responseMock.getResponse(request)
            println "<<< $response"
            response
        }
        // gRPC
        burrowQuery = Mock(BurrowQuery)
        burrowQuery.getAccount(_ as RpcQuery.GetAccountParam) >> { RpcQuery.GetAccountParam request ->
            println ">>> $request"
            Acm.Account response = Acm.Account.newBuilder().build()
            println "<<< $response"
            response
        }
        burrowTransact = Mock(BurrowTransact)
        burrowTransact.callTx(_ as Payload.CallTx) >> { Payload.CallTx request ->
            println ">>> $request"
            Exec.Result result = Exec.Result.newBuilder().setReturn(HexValue.copyFrom('')).build()
            Exec.TxExecution response = Exec.TxExecution.newBuilder().setResult(result).build()
            println "<<< $response"
            response
        }
        burrowEvents = Mock(BurrowEvents)
        burrowEvents.getEvents(_ as RpcEvents.BlocksRequest, _ as StreamObserver<RpcEvents.EventsResponse>) >> { RpcEvents.BlocksRequest request, StreamObserver<RpcEvents.EventsResponse> observer ->
            println ">>> $request"
            long height = 108
            RpcEvents.EventsResponse value = RpcEvents.EventsResponse.newBuilder().setHeight(height).addEvents(
                Exec.Event.newBuilder().setHeader(
                    Exec.Header.newBuilder().setTxType(2).setTxHash(HexValue.copyFrom('0xa8af028a6aa5a15ffbc6bd80795e0731f7f0b4f2777b4ea006ed97e878e1aaec')).setEventType(2).setEventID("Log/32B11B5AE572F59C0345223EC2403B7A91FD2DA2").setHeight(height).build()
                ).setLog(
                    Exec.LogEvent.newBuilder().setAddress(HexValue.copyFrom('0x32B11B5AE572F59C0345223EC2403B7A91FD2DA2')).setData(HexValue.copyFrom('0x0000000000000000000000000c1bf03b1b90ac16a60349495b907eb5ff213507')).addTopics(HexValue.copyFrom('0xb123f68b8ba02b447d91a6629e121111b7dd6061ff418a60139c8bf00522a284')).build()
                ).build()
            ).build()
            println "<<< $value"
        }
    }

    def "test contract function to ns node conversion"() {
        given: 'the contracts can be retrieved from the contract registry'
        Map<String, FlowSvcImpl> functions = Util.instance().getFunctions(false)

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
        Map<String,Event> events = Util.instance().getEvents(false)

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
        IData[] contractAddresses = Util.instance().getContractAddresses()

        expect: 'to retrieve a populated list of contract address mappings'
        contractAddresses.length == 2

        when: 'a particular item is retrieved from the list'
        IDataMap contract = new IDataMap(contractAddresses[0])

        then: 'the values are as expected'
        contract.get('uri') == 'sample/util/Console'
        contract.get('address') == null
    }

    def "test call log"(ServiceSupplier serviceProvider) {
        given: 'the needed instances and handling of service layer communications'
        NSName nsName = NSName.create('sample.SimpleStorage:log')
        Util.instance().serviceSupplier = serviceProvider

        when: 'the contract address is remembered; implying the contract was deployed'
        if (!Util.instance().isContractDeployed(nsName)) {
            Util.instance().storeContractAddress(nsName, responseMock.contractAddress)
        }

        and: 'we attempt to get the log observable'
        IData pipeline = IDataFactory.create()
        Util.instance().call(nsName, pipeline)

        then: 'a valid instance is retrieved'
        pipeline == IDataFactory.create()

        where: 'the service provider is from the list of supported providers'
        serviceProvider << [
            new ServiceSupplierWeb3(Web3Service.build(web3)),
            new ServiceSupplierBurrow(burrowQuery, burrowTransact, burrowEvents)
        ]
    }

    @Unroll def 'test log event #serviceProvider.class.name'(List filterChanges, Object observer, ServiceSupplier serviceProvider) {
        given: 'the needed instances and handling of service layer communications'
        NSName nsName = NSName.create('sample.SimpleStorage:LogAddress')
        Util.instance().serviceSupplier = serviceProvider

        when: 'the contract address is remembered; implying the contract was deployed'
        if (!Util.instance().isContractDeployed(nsName)) {
            Util.instance().storeContractAddress(nsName, responseMock.contractAddress)
        }

        and: 'we attempt to get the log observable'
        Util.instance().getLogObservable(nsName, observer)

        and: 'the first log event is being decoded'
        Message msg = Util.instance().decodeLogEvent(nsName, filterChanges[0]) //TODO :: Generify

        then: 'the resulting pipeline has the expected values'
        msg.getIData() == IDataFactory.create((Object[][])[
            ['_env', IDataFactory.create((Object[][])[['uuid', '30']])],
            ['contractAddress', HexValue.toBigInteger('33F71BB66F8994DD099C0E360007D4DEAE11BFFE')]
        ])

        where: 'the service provider is from the list of supported providers'
        filterChanges                                   | observer                | serviceProvider
        responseMock.getExpectedFilterChangesWeb3()     | Mock(Observer)          | new ServiceSupplierWeb3(Web3Service.build(web3, 1000, Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())))
        responseMock.getExpectedFilterChangesBurrow()   | Mock(StreamObserver)    | new ServiceSupplierBurrow(burrowQuery, burrowTransact, burrowEvents)
    }
}