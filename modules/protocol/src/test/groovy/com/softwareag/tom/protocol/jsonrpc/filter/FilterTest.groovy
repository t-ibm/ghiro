/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.ByteString
import com.softwareag.tom.ObjectMapperFactory
import com.softwareag.tom.protocol.jsonrpc.JsonRpcRx
import com.softwareag.tom.protocol.jsonrpc.Request
import com.softwareag.tom.protocol.jsonrpc.Response
import com.softwareag.tom.protocol.jsonrpc.Service
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetFilterChanges
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthUninstallFilter
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetFilterChanges
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthUninstallFilter
import com.softwareag.tom.protocol.util.HexValue
import rx.Observable
import rx.observers.TestSubscriber
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * System under specification: {@link Filter}.
 * @author tglaeser
 */
class FilterTest extends FilterSpecification {

    def "test log filter observable"() {
        given: 'a subscriber and a list of valid JSON-RPC response'
        List<ResponseEthGetFilterChanges.Event> expected = responseEthGetFilterChanges.getEvents()
        TestSubscriber<ResponseEthGetFilterChanges.Log> testSubscriber = new TestSubscriber<>()

        when: 'the response are received'
        Observable<ResponseEthGetFilterChanges.Log> observable = jsonRpcRx.ethLogObservable(requestEthNewFilter, 1000)
        observable.subscribe(testSubscriber)
        List<ResponseEthGetFilterChanges.Event> results = testSubscriber.getOnNextEvents()

        then: 'the responses match the expectation'
        println "expected :: $expected"
        println "received :: $results"
        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(3)
        testSubscriber.assertValues(expected as ResponseEthGetFilterChanges.Log[])
        results == expected

        when: 'the subscription is terminated'
        testSubscriber.unsubscribe()

        then: 'the subscriber has been removed'
        testSubscriber.isUnsubscribed()
    }
}

abstract class FilterSpecification extends Specification {
    @Shared Service service
    @Shared JsonRpcRx jsonRpcRx
    @Shared ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    @Shared RequestEthNewFilter requestEthNewFilter
    @Shared ResponseEthGetFilterChanges responseEthGetFilterChanges

    def setup() {
        service = Mock(Service)
        jsonRpcRx = new JsonRpcRx(service, scheduledExecutorService)

        ByteString contractAddress = HexValue.toByteString('00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE')
        ByteString filterId = HexValue.toByteString('F3449755BA20C7BB6DB1B3433C2172096AA0033DABF7FD43388A3110BC8BEA5D')

        requestEthNewFilter = new RequestEthNewFilter(service, contractAddress)

        ObjectMapper objectMapper = ObjectMapperFactory.getJsonMapper()
        String responseEthNewFilterContent = '{"id":42, "jsonrpc":"2.0", "result":{"sub_id":"' + filterId + '"}}'
        String responseEthGetFilterChangesContent = '{"id":42, "jsonrpc":"2.0", "result":{events:[{"address":"' + contractAddress + '", "data":"0000000000000000000000000000000000000000000000000000000000000001", "height":30, "topics":["88C4F556FDC50387EC6B6FC4E8250FECC56FF50E873DF06DADEEB84C0287CA90", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]},' +
                                                                                                 '{"address":"' + contractAddress + '", "data":"0000000000000000000000000000000000000000000000000000000000000001", "height":30, "topics":["88C4F556FDC50387EC6B6FC4E8250FECC56FF50E873DF06DADEEB84C0287CA90", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]},' +
                                                                                                 '{"address":"' + contractAddress + '", "data":"0000000000000000000000000000000000000000000000000000000000000001", "height":30, "topics":["88C4F556FDC50387EC6B6FC4E8250FECC56FF50E873DF06DADEEB84C0287CA90", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]}]}}'
        String responseEthUninstallFilterContent = '{"id":42, "jsonrpc":"2.0", "result":{"result":"true"}}'

        ResponseEthNewFilter responseEthNewFilter = objectMapper.readValue(responseEthNewFilterContent, ResponseEthNewFilter.class)
        responseEthGetFilterChanges = objectMapper.readValue(responseEthGetFilterChangesContent, ResponseEthGetFilterChanges.class)
        ResponseEthUninstallFilter responseEthUninstallFilter = objectMapper.readValue(responseEthUninstallFilterContent, ResponseEthUninstallFilter.class)

        service.send(_ as Request, _ as Class) >> { Request r, Class c ->
            println ">>> $r"
            Response response
            if (r instanceof RequestEthNewFilter) {
                response = responseEthNewFilter
            } else if (r instanceof RequestEthGetFilterChanges) {
                response = responseEthGetFilterChanges
            } else if (r instanceof RequestEthUninstallFilter) {
                response = responseEthUninstallFilter
            } else {
                response = null
            }
            println "<<< $response"
            response
        }
    }
}