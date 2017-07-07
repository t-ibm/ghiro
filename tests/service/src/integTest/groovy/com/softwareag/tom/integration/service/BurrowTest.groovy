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
        Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(ByteString.copyFromUtf8("E9B5D87313356465FAE33C406CE2C2979DE60BCB")).build()
        Message response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'we receive a valid response'
        response instanceof Types.ResponseEthGetBalance
        ((Types.ResponseEthGetBalance) response).getBalance() == 200000000
    }
}