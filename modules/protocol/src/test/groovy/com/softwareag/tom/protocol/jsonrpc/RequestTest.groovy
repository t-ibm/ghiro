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
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthSendTransaction
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
        RequestWeb3ClientVersion request = new RequestWeb3ClientVersion(serviceHttp, Types.RequestWeb3ClientVersion.newBuilder().build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.getClientVersion","params":{},"id":"1"}'

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == expected
    }

    def "test net_listening"() {
        given: 'a valid request type'
        RequestNetListening request = new RequestNetListening(serviceHttp, Types.RequestNetListening.newBuilder().build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.isListening","params":{},"id":"1"}'

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == expected
    }

    def "test eth_getBalance"() {
        when: 'a valid request type is created'
        RequestEthGetBalance request = new RequestEthGetBalance(serviceHttp, Types.RequestEthGetBalance.newBuilder().setAddress(ByteString.copyFromUtf8("E9B5D87313356465FAE33C406CE2C2979DE60BCB")).build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.getAccount","params":{"address":"E9B5D87313356465FAE33C406CE2C2979DE60BCB"},"id":"1"}'

        then: 'the expected request object is created'
        request.params.address == 'E9B5D87313356465FAE33C406CE2C2979DE60BCB'

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == expected
    }

    def "test eth_sendTransaction"() {
        when: 'a valid request type is created'
        RequestEthSendTransaction request = new RequestEthSendTransaction(serviceHttp, Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(ByteString.copyFromUtf8("606060")).setGas(12).setGasPrice(223).build()
        ).build()) {};
        String expected = '{"jsonrpc":"2.0","method":"burrow.transactAndHold","params":{"priv_key":"6B72D45EB65F619F11CE580C8CAED9E0BADC774E9C9C334687A65DCBAD2C4151CB3688B7561D488A2A4834E1AEE9398BEF94844D8BDBBCA980C11E3654A45906","address":"","data":"606060","fee":12,"gas_limit":223},"id":"1"}'

        then: 'the expected request object is created'
        request.params.privKey == '6B72D45EB65F619F11CE580C8CAED9E0BADC774E9C9C334687A65DCBAD2C4151CB3688B7561D488A2A4834E1AEE9398BEF94844D8BDBBCA980C11E3654A45906'
        request.params.address == ''
        request.params.data == '606060'
        request.params.fee == 12
        request.params.gasLimit == 223

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == expected
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
            println actual
            response
        }
    }
}