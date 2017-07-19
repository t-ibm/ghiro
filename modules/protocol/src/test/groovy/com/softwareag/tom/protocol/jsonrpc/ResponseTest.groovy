/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc

import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthCall
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetBalance
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetFilterChanges
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetStorageAt
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthSendTransaction
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
class ResponseTest extends ResponseSpecification {

    def "test error"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "error":{"code":-32603, "message":"Internal JSON-RPC error"}}'

        when: 'the response is received'
        ResponseWeb3ClientVersion response = serviceHttp.getResponseHandler(ResponseWeb3ClientVersion.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.id == 42
        response.jsonrpc == '2.0'
        response.error.code == -32603
        response.error.message == 'Internal JSON-RPC error'
        response.error.data == null
    }

    def "test web3_clientVersion"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "result":{"client_version":"0.8.0"}}'

        when: 'the response is received'
        ResponseWeb3ClientVersion response = serviceHttp.getResponseHandler(ResponseWeb3ClientVersion.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.error == null
        response.result.clientVersion == '0.8.0'
    }

    def "test net_listening"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "result":{"listening":true}}'

        when: 'the response is received'
        ResponseNetListening response = serviceHttp.getResponseHandler(ResponseNetListening.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.error == null
        response.result.listening
    }

    def "test eth_getBalance"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "result":{"address":"E9B5D87313356465FAE33C406CE2C2979DE60BCB", "balance":200000000, "code":"", "pub_key":null, "sequence":0, "storage_root":""}}'

        when: 'the response is received'
        ResponseEthGetBalance response = serviceHttp.getResponseHandler(ResponseEthGetBalance.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.error == null
        response.result.balance == 200000000
    }

    def "test eth_getStorageAt"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "result":{"key":"", "value":""}}'

        when: 'the response is received'
        ResponseEthGetStorageAt response = serviceHttp.getResponseHandler(ResponseEthGetStorageAt.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.error == null
        response.result.value == ''
    }

    def "test eth_sendTransaction"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "result":{"call_data":{"callee":"3F2F648518AE519964315B9B54ECD8FE23E6075F", "caller":"37236DF251AB70022B1DA351F08A20FB52443E37", "data":"606060", "gas":208, "value":0}, "exception":"", "origin":"37236DF251AB70022B1DA351F08A20FB52443E37", "return":"606060", "tx_id":"619DB1BBEC212208EF9949D5F341722B0312219C"}}'

        when: 'the response is received'
        ResponseEthSendTransaction response = serviceHttp.getResponseHandler(ResponseEthSendTransaction.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.error == null
        response.result.txId == '619DB1BBEC212208EF9949D5F341722B0312219C'
    }

    def "test eth_call"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "result":{"gas_used":49, "return":""}}'

        when: 'the response is received'
        ResponseEthCall response = serviceHttp.getResponseHandler(ResponseEthCall.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.error == null
        response.result.ret == ''
    }

    def "test eth_newFilter"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "result":{"sub_id":"E8BD53B1A38F5C3A1A3C38640327A41677BC7759763150D5138F7CBE7A361E5F"}}'

        when: 'the response is received'
        ResponseEthNewFilter response = serviceHttp.getResponseHandler(ResponseEthNewFilter.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.error == null
        response.result.subId == 'E8BD53B1A38F5C3A1A3C38640327A41677BC7759763150D5138F7CBE7A361E5F'
    }

    def "test eth_getFilterChanges"() {
        given: 'a valid JSON-RPC response'
        content '{"id":42, "jsonrpc":"2.0", "result":{events:[{"address":"00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE", "data":"0000000000000000000000000000000000000000000000000000000000000001", "height":30, "topics":["88C4F556FDC50387EC6B6FC4E8250FECC56FF50E873DF06DADEEB84C0287CA90", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]},' +
                                                             '{"address":"00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE", "data":"0000000000000000000000000000000000000000000000000000000000000001", "height":30, "topics":["88C4F556FDC50387EC6B6FC4E8250FECC56FF50E873DF06DADEEB84C0287CA90", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]},' +
                                                             '{"address":"00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE", "data":"0000000000000000000000000000000000000000000000000000000000000001", "height":30, "topics":["88C4F556FDC50387EC6B6FC4E8250FECC56FF50E873DF06DADEEB84C0287CA90", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]}]}}'

        when: 'the response is received'
        ResponseEthGetFilterChanges response = serviceHttp.getResponseHandler(ResponseEthGetFilterChanges.class).handleResponse(closeableHttpResponse);

        then: 'the response type values are set to the expected values'
        response.error == null
        response.result.events.get(1).address == '00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE'
        response.result.events.get(1).data == '0000000000000000000000000000000000000000000000000000000000000001'
        response.result.events.get(1).height == 30
        response.result.events.get(1).topics.size() == 3
    }
}

abstract class ResponseSpecification extends Specification {
    String content
    @Shared CloseableHttpClient closeableHttpClient
    @Shared CloseableHttpResponse closeableHttpResponse
    @Shared HttpEntity httpEntity
    @Shared ServiceHttp serviceHttp

    def setupSpec() {
        closeableHttpClient = Mock(CloseableHttpClient)
        serviceHttp = new ServiceHttp('', closeableHttpClient)
    }

    def setup() {
        closeableHttpResponse = Mock(CloseableHttpResponse)
        httpEntity = Mock(HttpEntity)
        closeableHttpResponse.statusLine >> new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "Test")
        closeableHttpResponse.entity >> httpEntity
        httpEntity.content >> {
            println content
            new ByteArrayInputStream(content.getBytes())
        }
    }

    protected void content(String content) {
        this.content = content
    }
}