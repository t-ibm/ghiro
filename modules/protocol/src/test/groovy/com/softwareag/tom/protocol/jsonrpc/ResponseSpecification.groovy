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
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthUninstallFilter
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
import spock.lang.Unroll

/**
 * System under specification: {@link Response}.
 * @author tglaeser
 */
class ResponseSpecification extends ResponseBaseSpecification {

    @Unroll def "test error with #type"() {
        given: 'a valid JSON-RPC response'
        Response expected = type.newInstance([-32603, "Internal JSON-RPC error"] as Object[])
        content expected.toString()

        when: 'the response is received'
        Response response = serviceHttp.getResponseHandler(type).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected

        where:
        type << [
                ResponseEthCall, ResponseEthGetBalance, ResponseEthGetFilterChanges, ResponseEthGetStorageAt, ResponseEthNewFilter, ResponseEthSendTransaction, ResponseEthUninstallFilter, ResponseNetListening, ResponseWeb3ClientVersion
        ]
    }

    def "test web3_clientVersion"() {
        given: 'a valid JSON-RPC response'
        ResponseWeb3ClientVersion expected = new ResponseWeb3ClientVersion("0.8.0")
        content expected.toString()

        when: 'the response is received'
        ResponseWeb3ClientVersion response = serviceHttp.getResponseHandler(ResponseWeb3ClientVersion.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }

    def "test net_listening"() {
        given: 'a valid JSON-RPC response'
        ResponseNetListening expected = new ResponseNetListening(true)
        content expected.toString()

        when: 'the response is received'
        ResponseNetListening response = serviceHttp.getResponseHandler(ResponseNetListening.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }

    def "test eth_getBalance"() {
        given: 'a valid JSON-RPC response'
        ResponseEthGetBalance expected = new ResponseEthGetBalance("E9B5D87313356465FAE33C406CE2C2979DE60BCB", 200000000)
        content expected.toString()

        when: 'the response is received'
        ResponseEthGetBalance response = serviceHttp.getResponseHandler(ResponseEthGetBalance.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }

    def "test eth_getStorageAt"() {
        given: 'a valid JSON-RPC response'
        ResponseEthGetStorageAt expected = new ResponseEthGetStorageAt("", "")
        content expected.toString()

        when: 'the response is received'
        ResponseEthGetStorageAt response = serviceHttp.getResponseHandler(ResponseEthGetStorageAt.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }

    def "test eth_sendTransaction"() {
        given: 'a valid JSON-RPC response'
        ResponseEthSendTransaction expected = new ResponseEthSendTransaction("3F2F648518AE519964315B9B54ECD8FE23E6075F", "37236DF251AB70022B1DA351F08A20FB52443E37", "606060", "619DB1BBEC212208EF9949D5F341722B0312219C", 208)
        content expected.toString()

        when: 'the response is received'
        ResponseEthSendTransaction response = serviceHttp.getResponseHandler(ResponseEthSendTransaction.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }

    def "test eth_call"() {
        given: 'a valid JSON-RPC response'
        ResponseEthCall expected = new ResponseEthCall(49 as long, "")
        content expected.toString()

        when: 'the response is received'
        ResponseEthCall response = serviceHttp.getResponseHandler(ResponseEthCall.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }

    def "test eth_newFilter"() {
        given: 'a valid JSON-RPC response'
        ResponseEthNewFilter expected = new ResponseEthNewFilter("E8BD53B1A38F5C3A1A3C38640327A41677BC7759763150D5138F7CBE7A361E5F")
        content expected.toString()

        when: 'the response is received'
        ResponseEthNewFilter response = serviceHttp.getResponseHandler(ResponseEthNewFilter.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }

    def "test eth_uninstallFilter"() {
        given: 'a valid JSON-RPC response'
        ResponseEthUninstallFilter expected = new ResponseEthUninstallFilter(true)
        content expected.toString()

        when: 'the response is received'
        ResponseEthUninstallFilter response = serviceHttp.getResponseHandler(ResponseEthUninstallFilter.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }

    def "test eth_getFilterChanges"() {
        given: 'a valid JSON-RPC response'
        ResponseEthGetFilterChanges.Log logEvent = new ResponseEthGetFilterChanges.Log("00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE", "0000000000000000000000000000000000000000000000000000000000000001", 30, ["88C4F556FDC50387EC6B6FC4E8250FECC56FF50E873DF06DADEEB84C0287CA90", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"])
        ResponseEthGetFilterChanges expected = new ResponseEthGetFilterChanges([logEvent, logEvent, logEvent])
        content expected.toString()

        when: 'the response is received'
        ResponseEthGetFilterChanges response = serviceHttp.getResponseHandler(ResponseEthGetFilterChanges.class).handleResponse(closeableHttpResponse)

        then: 'the response type values are set to the expected values'
        response.hashCode() == expected.hashCode()
        response == expected
    }
}

/**
 * A base specification providing support for common JSON-RPC responses.
 */
abstract class ResponseBaseSpecification extends Specification {
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