/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.service

import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.extension.Node
import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.jsonrpc.ServiceHttp
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link Web3Service}.
 * @author tglaeser
 */
class BurrowTest extends Specification {

    @Shared @Node protected ConfigObject config
    @Shared protected Web3Service web3Service

    def setup() {
        given: 'a JSON-RPC client'
        web3Service = Web3Service.build(new ServiceHttp("http://${config.node.host.ip}:${config.node.host.port}/rpc"));
    }

    public "test 'web3ClientVersion' service"() {
        when: 'we make a get request'
        Types.RequestWeb3ClientVersion request = Types.RequestWeb3ClientVersion.newBuilder().build()
        Message response = web3Service.web3ClientVersion(request)
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'we receive a valid response'
        response instanceof Types.ResponseWeb3ClientVersion
        ((Types.ResponseWeb3ClientVersion)response).clientVersion == '0.8.0'
    }

    public "test 'netListening' service"() {
        when: 'we make a get request'
        Types.RequestNetListening request = Types.RequestNetListening.newBuilder().build()
        Message response = web3Service.netListening(request)
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'we receive a valid response'
        response instanceof Types.ResponseNetListening
        ((Types.ResponseNetListening) response).getListening()
    }

    public "test 'ethGetBalance' service"() {
        when: 'we make a get request'
        Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(ByteString.copyFromUtf8("F60D30722E7B497FA532FB3207C3FB29C31B1992")).build()
        Message response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'we receive a valid response'
        response instanceof Types.ResponseEthGetBalance
        ((Types.ResponseEthGetBalance) response).getBalance() == 200000000
    }

    public "test 'ethSendTransaction' service"() {
        when: 'we make a get request'
        String contract = '6060604052608f8060106000396000f360606040523615600d57600d565b608d5b7f68616861000000000000000000000000000000000000000000000000000000007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f88c4f556fdc50387ec6b6fc4e8250fecc56ff50e873df06dadeeb84c0287ca9060016040518082815260200191505060405180910390a35b565b00'
        Types.RequestEthSendTransaction request = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(ByteString.copyFromUtf8(contract)).setGas(12).setGasPrice(223).build()
        ).build()
        Message response = web3Service.ethSendTransaction(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'we receive a valid response'
        response instanceof Types.ResponseEthSendTransaction
        ((Types.ResponseEthSendTransaction) response).getHash() != null
    }

    public "test 'ethCall' service"() {
        when: 'we make a get request'
        Types.RequestEthCall request = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(ByteString.copyFromUtf8("33F71BB66F8994DD099C0E360007D4DEAE11BFFE")).build()
        ).build()
        Message response
        2.times {
            response = web3Service.ethCall(request)
            println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"
        }

        then: 'we receive a valid response'
        response instanceof Types.ResponseEthCall
        ((Types.ResponseEthCall) response).getReturn() != null
    }
}