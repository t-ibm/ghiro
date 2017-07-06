/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc

import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.request.RequestNetListening
import com.softwareag.tom.protocol.jsonrpc.request.RequestWeb3ClientVersion
import com.softwareag.tom.protocol.jsonrpc.response.ResponseNetListening
import com.softwareag.tom.protocol.jsonrpc.response.ResponseWeb3ClientVersion
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link Request}.
 * @author tglaeser
 */
class RequestTest extends Specification {

    CloseableHttpClient closeableHttpClient
    @Shared ServiceHttp serviceHttp

    def setup() {
        closeableHttpClient = Mock(CloseableHttpClient)
        serviceHttp = new ServiceHttp('', closeableHttpClient)
    }

    def "test web3_clientVersion"() {
        given: 'a valid request type'
        Request request = new RequestWeb3ClientVersion<String, ResponseWeb3ClientVersion>(serviceHttp, Types.RequestWeb3ClientVersion.newBuilder().build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.getClientVersion","params":[],"id":"1"}'

        and: 'the base fields are accessible with their default values'
        request.jsonrpc = Request.JSONRPC_VERSION
        request.method = null
        request.params = null
        request.id = null

        when: 'the request is send'
        request.send()

        then: 'a valid JSON-RPC request is created'
        closeableHttpClient.execute(_ as HttpPost, _ as ResponseHandler) >> { HttpPost httpPost, ResponseHandler responseHandler ->
            httpPost.entity.content.text == expected
            new ResponseWeb3ClientVersion()
        }
    }

    def "test net_listening"() {
        given: 'a valid request type'
        Request request = new RequestNetListening<String, ResponseNetListening>(serviceHttp, Types.RequestNetListening.newBuilder().build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.isListening","params":[],"id":"1"}'

        when: 'the request is send'
        request.send()

        then: 'a valid JSON-RPC request is created'
        closeableHttpClient.execute(_ as HttpPost, _ as ResponseHandler) >> { HttpPost httpPost, ResponseHandler responseHandler ->
            httpPost.entity.content.text == expected
            new ResponseNetListening()
        }
    }
}