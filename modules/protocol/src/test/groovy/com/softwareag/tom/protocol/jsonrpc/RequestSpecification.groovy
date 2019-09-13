/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc

import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.request.ParamsAddress
import com.softwareag.tom.protocol.jsonrpc.request.ParamsAddressData
import com.softwareag.tom.protocol.jsonrpc.request.ParamsAddressDataTx
import com.softwareag.tom.protocol.jsonrpc.request.ParamsEvent
import com.softwareag.tom.protocol.jsonrpc.request.ParamsFilter
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthCall
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetBalance
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetFilterChanges
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetStorageAt
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewBlockFilter
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthSendTransaction
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthUninstallFilter
import com.softwareag.tom.protocol.jsonrpc.request.RequestNetListening
import com.softwareag.tom.protocol.jsonrpc.request.RequestWeb3ClientVersion
import com.softwareag.tom.protocol.util.HexValue
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link Request}.
 * @author tglaeser
 */
class RequestSpecification extends RequestBaseSpecification {

    def "test request base"() {
        given: 'a valid request type'
        Request defaultRequest = new RequestWeb3ClientVersion(serviceHttp)
        Request request = new RequestWeb3ClientVersion(serviceHttp)

        when: 'the base fields are accessible and set to their default values'
        request.jsonrpc = Service.JSONRPC_VERSION
        request.method = 'burrow.getClientVersion'
        request.params = [:]
        request.id = 1

        then: 'the two instances are identical'
        request.jsonrpc == defaultRequest.jsonrpc
        request.method == defaultRequest.method
        request.params == defaultRequest.params
        request.id == defaultRequest.id
        request.hashCode() == defaultRequest.hashCode()
        request == defaultRequest
    }

    def "test web3_clientVersion"() {
        given: 'a valid request type'
        RequestWeb3ClientVersion request = new RequestWeb3ClientVersion(serviceHttp) {}

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test net_listening"() {
        given: 'a valid request type'
        RequestNetListening request = new RequestNetListening(serviceHttp) {}

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test eth_getBalance"() {
        given: 'a text fixture'
        String address = "E9B5D87313356465FAE33C406CE2C2979DE60BCB"
        ParamsAddress params = new ParamsAddress(address)

        when: 'a valid request type is created'
        RequestEthGetBalance request = new RequestEthGetBalance(serviceHttp, Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(address)).build()) {}

        then: 'the expected request object is created'
        request.params.hashCode() == params.hashCode()
        request.params == params

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test eth_getStorageAt"() {
        given: 'a text fixture'
        String address = "E9B5D87313356465FAE33C406CE2C2979DE60BCB"
        ParamsAddress params = new ParamsAddress(address)

        when: 'a valid request type is created'
        RequestEthGetStorageAt request = new RequestEthGetStorageAt(serviceHttp, Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(address)).build()) {}

        then: 'the expected request object is created'
        request.params.hashCode() == params.hashCode()
        request.params == params

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test eth_sendTransaction"() {
        given: 'a text fixture'
        String address = '33F71BB66F8994DD099C0E360007D4DEAE11BFFE'
        String data = '606060'
        long fee = 12
        long gasLimit = 223
        ParamsAddressDataTx params = new ParamsAddressDataTx(address, data, fee, gasLimit)

        when: 'a valid request type is created'
        RequestEthSendTransaction request = new RequestEthSendTransaction(serviceHttp, Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(address)).setData(HexValue.toByteString(data)).setGas(HexValue.toByteString(gasLimit)).setGasPrice(HexValue.toByteString(fee)).build()
        ).build()) {}

        then: 'the expected request object is created'
        request.params.hashCode() == params.hashCode()
        request.params == params

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test eth_call"() {
        given: 'a text fixture'
        String address = '33F71BB66F8994DD099C0E360007D4DEAE11BFFE'
        String data = '606060'
        ParamsAddressData params = new ParamsAddressData(address, data)

        when: 'a valid request type is created'
        RequestEthCall request = new RequestEthCall(serviceHttp, Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(address)).setData(HexValue.toByteString(data)).build()
        ).build()) {}

        then: 'the expected request object is created'
        request.params.hashCode() == params.hashCode()
        request.params == params

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test eth_newFilter"() {
        given: 'a text fixture'
        String address = '33F71BB66F8994DD099C0E360007D4DEAE11BFFE'
        ParamsEvent params = new ParamsEvent("Log/$address")

        when: 'a valid request type is created'
        RequestEthNewFilter request = new RequestEthNewFilter(serviceHttp, Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(address)).build()
        ).build()) {}

        then: 'the expected request object is created'
        request.params.hashCode() == params.hashCode()
        request.params == params

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test eth_newBlockFilter"() {
        given: 'a text fixture'
        ParamsEvent params = new ParamsEvent("NewBlock")

        when: 'a valid request type is created'
        RequestEthNewBlockFilter request = new RequestEthNewBlockFilter(serviceHttp) {}

        then: 'the expected request object is created'
        request.params.hashCode() == params.hashCode()
        request.params == params

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test eth_uninstallFilter"() {
        given: 'a text fixture'
        String filterId = '11FADF899CA265DCE0D2071C5CC3F317ADA94930D837F597B440B3BCB9291164'
        ParamsFilter params = new ParamsFilter(filterId)

        when: 'a valid request type is created'
        RequestEthUninstallFilter request = new RequestEthUninstallFilter(serviceHttp, Types.RequestEthUninstallFilter.newBuilder().setId(
                HexValue.toByteString(filterId)
        ).build()) {}

        then: 'the expected request object is created'
        request.params.hashCode() == params.hashCode()
        request.params == params

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }

    def "test eth_getFilterChanges"() {
        given: 'a text fixture'
        String filterId = '11FADF899CA265DCE0D2071C5CC3F317ADA94930D837F597B440B3BCB9291164'
        ParamsFilter params = new ParamsFilter(filterId)

        when: 'a valid request type is created'
        RequestEthGetFilterChanges request = new RequestEthGetFilterChanges(serviceHttp, Types.RequestEthGetFilterChanges.newBuilder().setId(
                HexValue.toByteString(filterId)
        ).build()) {}

        then: 'the expected request object is created'
        request.params.hashCode() == params.hashCode()
        request.params == params

        when: 'the request is send'
        request.send()

        then: 'the expected JSON-RPC request is created'
        actual == request.toString()
    }
}

/**
 * A base specification providing support for common JSON-RPC requests.
 */
abstract class RequestBaseSpecification extends Specification {
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