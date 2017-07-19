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

        then: 'a valid response is received'
        response instanceof Types.ResponseWeb3ClientVersion
        ((Types.ResponseWeb3ClientVersion)response).clientVersion == '0.8.0'
    }

    public "test 'netListening' service"() {
        when: 'we make a get request'
        Types.RequestNetListening request = Types.RequestNetListening.newBuilder().build()
        Message response = web3Service.netListening(request)
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseNetListening
        ((Types.ResponseNetListening) response).getListening()
    }

    public "test 'ethGetBalance' service"() {
        when: 'we make a get request'
        Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(ByteString.copyFromUtf8("F60D30722E7B497FA532FB3207C3FB29C31B1992")).build()
        Message response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetBalance
        ((Types.ResponseEthGetBalance) response).getBalance() == 200000000
    }

    public "test create_and_solidity_event services"() {
        given: 'a valid Solidity contract'
        String contract = '6060604052608f8060106000396000f360606040523615600d57600d565b608d5b7f68616861000000000000000000000000000000000000000000000000000000007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f88c4f556fdc50387ec6b6fc4e8250fecc56ff50e873df06dadeeb84c0287ca9060016040518082815260200191505060405180910390a35b565b00'
        def callee = [
                address:'33F71BB66F8994DD099C0E360007D4DEAE11BFFE',
                priv_key:'4487A3ED876CE4BB95C5E4982E5EB64BA4FADE2E7F1125F80F910EB9BE78DB48CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20'
        ]

        when: println '(1) the transaction is fully processed'
        Message request = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(ByteString.copyFromUtf8(contract)).setGas(12).setGasPrice(223).build()
        ).build()
        Message response = web3Service.ethSendTransaction(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthSendTransaction
        ((Types.ResponseEthSendTransaction) response).getHash() != null

        when: println '(2) the newly created contract account is verified'
        request = Types.RequestEthGetBalance.newBuilder().setAddress(ByteString.copyFromUtf8(callee.address)).build()
        response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response\n"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetBalance
        ((Types.ResponseEthGetBalance) response).getBalance() == 0

        when: println '(3) the storage of the contract is retrieved'
        request = Types.RequestEthGetStorageAt.newBuilder().setAddress(ByteString.copyFromUtf8(callee.address)).build()
        response = web3Service.ethGetStorageAt(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response\n"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetStorageAt
        ((Types.ResponseEthGetStorageAt) response).getValue() == ByteString.copyFromUtf8('')

        when: println '(4) we subscribe to events from the the new contract account'
        request = Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(ByteString.copyFromUtf8(callee.address)).build()
        ).build()
        response = web3Service.ethNewFilter(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        and: 'the filter id is remembered'
        def filterId = ((Types.ResponseEthNewFilter) response).getFilterId()

        then: 'a valid response is received'
        response instanceof Types.ResponseEthNewFilter
        ((Types.ResponseEthNewFilter) response).getFilterId().size() == 32*2

        when: println '(5) we poll for events'
        request = Types.RequestEthGetFilterChanges.newBuilder().setFilterId(filterId).build()
        response = web3Service.ethGetFilterChanges(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response\n"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetFilterChanges
        ((Types.ResponseEthGetFilterChanges) response).getLogCount() == 0

        when: println '(6) the contract is executed 2 times'
        request = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(ByteString.copyFromUtf8(callee.address)).build()
        ).build()
        2.times {
            response = web3Service.ethCall(request as Types.RequestEthCall)
            println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"
        }

        then: 'a valid response is received'
        response instanceof Types.ResponseEthCall
        ((Types.ResponseEthCall) response).getReturn() != null

        when: println '\n(7) we poll for events again'
        request = Types.RequestEthGetFilterChanges.newBuilder().setFilterId(filterId).build()
        response = web3Service.ethGetFilterChanges(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetFilterChanges
        ((Types.ResponseEthGetFilterChanges) response).getLogCount() == 2
        ((Types.ResponseEthGetFilterChanges) response).getLog(1).address.size() == 32*2
        ((Types.ResponseEthGetFilterChanges) response).getLog(1).data.size() == 32*2
    }
}