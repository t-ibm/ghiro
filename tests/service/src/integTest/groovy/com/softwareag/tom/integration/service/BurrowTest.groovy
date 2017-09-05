/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.service

import com.google.protobuf.Message
import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.extension.Node
import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.jsonrpc.ServiceHttp
import com.softwareag.tom.protocol.util.HexValue
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
        ((Types.ResponseWeb3ClientVersion)response).version == '0.8.0'
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
        Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString("F60D30722E7B497FA532FB3207C3FB29C31B1992")).build()
        Message response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetBalance
        ((Types.ResponseEthGetBalance) response).getBalance() == HexValue.toByteString(200000000)
    }

    public "test create solidity contract and call event services"() {
        given: 'a valid Solidity contract'
        String contract = '606060405234610000575b610251806100196000396000f300606060405263ffffffff60e060020a60003504166351973ec9811461003a57806394e8767d14610049578063b60e72cc1461006b575b610000565b34610000576100476100c2565b005b346100005761005960043561010e565b60408051918252519081900360200190f35b3461000057610047600480803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843750949650509335935061016b92505050565b005b6040805173ffffffffffffffffffffffffffffffffffffffff3016815290517fb123f68b8ba02b447d91a6629e121111b7dd6061ff418a60139c8bf00522a2849181900360200190a15b565b600081151561013e57507f3000000000000000000000000000000000000000000000000000000000000000610164565b5b600082111561016457600a808304920660300160f860020a026101009091041761013f565b5b5b919050565b7f614c60883eb4fb2805fb25f372e0dd0572fbbd69edd43a764af7384df6785275826101968361010e565b604080516020808201849052828252845192820192909252835190918291606083019186019080838382156101e6575b8051825260208311156101e657601f1990920191602091820191016101c6565b505050905090810190601f1680156102125780820380516001836020036101000a031916815260200191505b50935050505060405180910390a15b50505600a165627a7a7230582080e6735eb8468c8ccb409bea165ee0b32ce38d4a1e206791d24d2c42720a3f870029'
        def callee = [
                address:'33F71BB66F8994DD099C0E360007D4DEAE11BFFE',
                priv_key:'4487A3ED876CE4BB95C5E4982E5EB64BA4FADE2E7F1125F80F910EB9BE78DB48CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20'
        ]

        when: println '(1) the transaction is fully processed'
        Message request = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(HexValue.toByteString(contract)).setGas(HexValue.toByteString(12)).setGasPrice(HexValue.toByteString(223)).build()
        ).build()
        Message response = web3Service.ethSendTransaction(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthSendTransaction
        ((Types.ResponseEthSendTransaction) response).getHash() != null

        when: println '(2) the newly created contract account is verified'
        request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(callee.address)).build()
        response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response\n"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetBalance
        ((Types.ResponseEthGetBalance) response).getBalance() == HexValue.toByteString(0)

        when: println '(3) the storage of the contract is retrieved'
        request = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(callee.address)).build()
        response = web3Service.ethGetStorageAt(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response\n"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetStorageAt
        ((Types.ResponseEthGetStorageAt) response).getValue() == HexValue.toByteString('')

        when: println '(4) we subscribe to events from the the new contract account'
        request = Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(callee.address)).build()
        ).build()
        response = web3Service.ethNewFilter(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        and: 'the filter id is remembered'
        def filterId = ((Types.ResponseEthNewFilter) response).getId()

        then: 'a valid response is received'
        response instanceof Types.ResponseEthNewFilter
        ((Types.ResponseEthNewFilter) response).getId().size() == 32*2+2

        when: println '(5) we poll for events'
        request = Types.RequestEthGetFilterChanges.newBuilder().setId(filterId).build()
        response = web3Service.ethGetFilterChanges(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response\n"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetFilterChanges
        ((Types.ResponseEthGetFilterChanges) response).getLogCount() == 0

        when: println '(6) the contract is executed 2 times'
        request = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(callee.address)).setData(HexValue.toByteString('51973ec9')).build()
        ).build()
        2.times {
            response = web3Service.ethCall(request as Types.RequestEthCall)
            println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"
        }

        then: 'a valid response is received'
        response instanceof Types.ResponseEthCall
        ((Types.ResponseEthCall) response).getReturn() != null

        when: println '\n(7) we poll for events again'
        request = Types.RequestEthGetFilterChanges.newBuilder().setId(filterId).build()
        response = web3Service.ethGetFilterChanges(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetFilterChanges
        ((Types.ResponseEthGetFilterChanges) response).getLogCount() == 2
        ((Types.ResponseEthGetFilterChanges) response).getLog(1).address.size() == 32*2+2
        ((Types.ResponseEthGetFilterChanges) response).getLog(1).data.size() == 32*2+2
    }
}