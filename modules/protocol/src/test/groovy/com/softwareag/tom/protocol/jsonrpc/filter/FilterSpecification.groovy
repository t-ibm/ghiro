/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.filter

import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.ResponseMock
import com.softwareag.tom.protocol.jsonrpc.JsonRpcRx
import com.softwareag.tom.protocol.jsonrpc.Request
import com.softwareag.tom.protocol.jsonrpc.Response
import com.softwareag.tom.protocol.jsonrpc.Service
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter
import com.softwareag.tom.protocol.util.HexValue
import rx.Observable
import rx.Observer
import rx.Subscription
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * System under specification: {@link Filter}.
 * @author tglaeser
 */
class FilterSpecification extends FilterBaseSpecification {

    def "test log observable"() {
        given: 'a subscriber and a list of valid JSON-RPC response'
        JsonRpcRx jsonRpcRx = new JsonRpcRx(service, Executors.newSingleThreadScheduledExecutor())
        RequestEthNewFilter requestEthNewFilter = new RequestEthNewFilter(service, HexValue.toByteString(responseMock.contractAddress))
        Observable<Types.FilterLogType> logObservable = jsonRpcRx.ethLogObservable(requestEthNewFilter, 1000)

        List<Types.FilterLogType> expected = responseMock.getExpectedFilterChanges()
        List<Types.FilterLogType> actual = []
        CountDownLatch transactionLatch = new CountDownLatch(expected.size())
        CountDownLatch completedLatch = new CountDownLatch(1)
        Subscription subscription = logObservable.subscribe([
            onCompleted: {
                completedLatch.countDown()
            },
            onError    : { Throwable e ->
                throw e
            },
            onNext     : { Types.FilterLogType result ->
                actual.add(result)
                transactionLatch.countDown()
            }
        ] as Observer)

        when: 'the events are received'
        transactionLatch.await(1, TimeUnit.SECONDS)

        then: 'the events match the expectation'
        println "expected :: $expected"
        println "received :: $actual"
        notThrown Throwable
        actual == expected

        when: 'the subscription is terminated'
        subscription.unsubscribe()
        completedLatch.await(1, TimeUnit.SECONDS)

        then: 'the subscriber has been removed'
        subscription.isUnsubscribed()
    }
}

abstract class FilterBaseSpecification extends Specification {
    @Shared Service service
    @Shared ResponseMock responseMock

    def setup() {
        service = Mock(Service)
        responseMock = new ResponseMock()
        service.send(_ as Request, _ as Class) >> { Request request, Class c ->
            println ">>> $request"
            Response response = responseMock.getResponse(request)
            println "<<< $response"
            response
        }
    }
}