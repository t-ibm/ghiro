/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc

import com.google.protobuf.ByteString
import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetBalance
import com.softwareag.tom.protocol.jsonrpc.request.RequestNetListening
import com.softwareag.tom.protocol.jsonrpc.request.RequestWeb3ClientVersion
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link Request}.
 * @author tglaeser
 */
class RequestTest extends RequestSpecification {

    def "test request base"() {
        given: 'a valid request type'
        Request defaultRequest = new RequestWeb3ClientVersion(serviceHttp, null)
        Request request = new RequestWeb3ClientVersion(serviceHttp, null)

        when: 'the base fields are accessible and set to their default values'
        request.jsonrpc = Request.JSONRPC_VERSION
        request.method = 'burrow.getClientVersion'
        request.params = [:]
        request.id = 1

        then: 'the two instances are identical'
        request.jsonrpc == defaultRequest.jsonrpc
        request.method == defaultRequest.method
        request.params == defaultRequest.params
        request.id == defaultRequest.id
        request == defaultRequest
    }

    def "test web3_clientVersion"() {
        given: 'a valid request type'
        Request request = new RequestWeb3ClientVersion(serviceHttp, Types.RequestWeb3ClientVersion.newBuilder().build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.getClientVersion","params":{},"id":"1"}'

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        compare expected
    }

    def "test net_listening"() {
        given: 'a valid request type'
        Request request = new RequestNetListening(serviceHttp, Types.RequestNetListening.newBuilder().build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.isListening","params":{},"id":"1"}'

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        compare expected
    }

    def "test eth_getBalance"() {
        given: 'a valid request type'
        Request request = new RequestEthGetBalance(serviceHttp, Types.RequestEthGetBalance.newBuilder().setAddress(ByteString.copyFromUtf8("E9B5D87313356465FAE33C406CE2C2979DE60BCB")).build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.getAccount","params":{"address":"E9B5D87313356465FAE33C406CE2C2979DE60BCB"},"id":"1"}'

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        compare expected
    }
}

abstract class RequestSpecification extends Specification {
    @Shared CloseableHttpClient closeableHttpClient
    @Shared ServiceHttp serviceHttp
    @Shared Response response
    String actual

    def setupSpec() {
        response = Mock(Response)
    }

    def setup() {
        closeableHttpClient = Mock(CloseableHttpClient)
        serviceHttp = new ServiceHttp('', closeableHttpClient)
        closeableHttpClient.execute(_ as HttpPost, _ as ResponseHandler) >> { HttpPost httpPost, ResponseHandler responseHandler ->
            actual = httpPost.entity.content.text
            response
        }
    }

    protected boolean compare(String expected) {
        println actual
        actual == expected
    }
}