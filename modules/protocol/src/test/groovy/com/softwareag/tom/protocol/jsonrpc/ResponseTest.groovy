/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc

import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetBalance
import com.softwareag.tom.protocol.jsonrpc.response.ResponseNetListening
import com.softwareag.tom.protocol.jsonrpc.response.ResponseWeb3ClientVersion
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.HttpVersion
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link Response}.
 * @author tglaeser
 */
class ResponseTest extends Specification {

    CloseableHttpClient closeableHttpClient
    CloseableHttpResponse closeableHttpResponse
    HttpEntity httpEntity
    @Shared ServiceHttp serviceHttp

    def setup() {
        closeableHttpClient = Mock(CloseableHttpClient)
        closeableHttpResponse = Mock(CloseableHttpResponse)
        httpEntity = Mock(HttpEntity)
        serviceHttp = new ServiceHttp('', closeableHttpClient)
    }

    def "test error"() {
        given: 'a valid JSON-RPC response'
        String input = '{"id":42, "jsonrpc":"2.0", "error":{"code":-32603, "message":"Internal JSON-RPC error"}}'
        println input

        when: 'the response is received'
        ResponseWeb3ClientVersion response = serviceHttp.getResponseHandler(ResponseWeb3ClientVersion.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        closeableHttpResponse.statusLine >> new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "Test")
        closeableHttpResponse.entity >> httpEntity
        httpEntity.content >> new ByteArrayInputStream(input.getBytes())
        response.id == 42
        response.jsonrpc == '2.0'
        response.error.code == -32603
        response.error.message == 'Internal JSON-RPC error'
        response.error.data == null
    }

    def "test web3_clientVersion"() {
        given: 'a valid JSON-RPC response'
        String input = '{"id":42, "jsonrpc":"2.0", "result":{"client_version":"0.8.0"}}'
        println input

        when: 'the response is received'
        ResponseWeb3ClientVersion response = serviceHttp.getResponseHandler(ResponseWeb3ClientVersion.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        closeableHttpResponse.statusLine >> new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "Test")
        closeableHttpResponse.entity >> httpEntity
        httpEntity.content >> new ByteArrayInputStream(input.getBytes())
        response.error == null
        response.result.clientVersion == '0.8.0'
    }

    def "test net_listening"() {
        given: 'a valid JSON-RPC response'
        String input = '{"id":42, "jsonrpc":"2.0", "result":{"listening":true}}'
        println input

        when: 'the response is received'
        ResponseNetListening response = serviceHttp.getResponseHandler(ResponseNetListening.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        closeableHttpResponse.statusLine >> new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "Test")
        closeableHttpResponse.entity >> httpEntity
        httpEntity.content >> new ByteArrayInputStream(input.getBytes())
        response.error == null
        response.result.listening
    }

    def "test eth_getBalance"() {
        given: 'a valid JSON-RPC response'
        String input = '{"id":42, "jsonrpc":"2.0", "result":{"address":"E9B5D87313356465FAE33C406CE2C2979DE60BCB", "balance":200000000, "code":"", "pub_key":null, "sequence":0, "storage_root":""}}'
        println input

        when: 'the response is received'
        ResponseEthGetBalance response = serviceHttp.getResponseHandler(ResponseEthGetBalance.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        closeableHttpResponse.statusLine >> new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "Test")
        closeableHttpResponse.entity >> httpEntity
        httpEntity.content >> new ByteArrayInputStream(input.getBytes())
        response.error == null
        response.result.balance == 200000000
    }
}