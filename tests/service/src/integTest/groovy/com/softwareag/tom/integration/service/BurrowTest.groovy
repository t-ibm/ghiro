/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.service

import com.google.protobuf.Message
import com.softwareag.tom.abi.ContractInterface
import com.softwareag.tom.abi.ContractRegistry
import com.softwareag.tom.abi.sol.SolidityLocationFileSystem
import com.softwareag.tom.extension.Node
import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.abi.Types
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
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem("${config.node.contract.registry.location}")).load()
        String contractBinary = contracts['sample/util/Console'].contractBinary
        ContractInterface contractAbi = contracts['sample/util/Console'].contractAbi
        List functions = contractAbi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification logFunction = functions.get(0)
        assert logFunction.name == 'log'

        def callee = [
                address:'33F71BB66F8994DD099C0E360007D4DEAE11BFFE',
                priv_key:'4487A3ED876CE4BB95C5E4982E5EB64BA4FADE2E7F1125F80F910EB9BE78DB48CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20'
        ]

        when: println '(1) the transaction is fully processed'
        Message request = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(HexValue.toByteString(contractBinary)).setGas(HexValue.toByteString(12)).setGasPrice(HexValue.toByteString(223)).build()
        ).build()
        Message response = web3Service.ethSendTransaction(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthSendTransaction
        ((Types.ResponseEthSendTransaction) response).getHash() != null

        when: println '(2) the newly created contract account is verified'
        request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(callee.address)).build()
        response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetBalance
        ((Types.ResponseEthGetBalance) response).getBalance() == HexValue.toByteString(0)

        when: println '(3) the storage of the contract is retrieved'
        request = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(callee.address)).build()
        response = web3Service.ethGetStorageAt(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetStorageAt
        ((Types.ResponseEthGetStorageAt) response).getValue() != null

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
                Types.TxType.newBuilder().setTo(HexValue.toByteString(callee.address)).setData(HexValue.toByteString(logFunction.encode([]))).build()
        ).build()
        2.times {
            response = web3Service.ethCall(request as Types.RequestEthCall)
            println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"
        }

        then: 'a valid response is received'
        response instanceof Types.ResponseEthCall
        ((Types.ResponseEthCall) response).getReturn() != null

        when: println '(7) we poll for events again'
        request = Types.RequestEthGetFilterChanges.newBuilder().setId(filterId).build()
        response = web3Service.ethGetFilterChanges(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetFilterChanges
        ((Types.ResponseEthGetFilterChanges) response).getLogCount() == 2
        ((Types.ResponseEthGetFilterChanges) response).getLog(1).address.size() == 32*2+2
        ((Types.ResponseEthGetFilterChanges) response).getLog(1).data.size() == 32*2+2
    }


    public "test create solidity contract and store/update data"() {
        given: 'a valid Solidity contract'
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem("${config.node.contract.registry.location}")).load()
        String contractBinary = contracts['sample/SimpleStorage'].contractBinary
        ContractInterface contractAbi = contracts['sample/SimpleStorage'].contractAbi
        List functions = contractAbi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification setFunction = functions.get(2)
        assert setFunction.name == 'set'
        ContractInterface.Specification getFunction = functions.get(3)
        assert getFunction.name == 'get'

        def callee = [
                address:'33F71BB66F8994DD099C0E360007D4DEAE11BFFE',
                priv_key:'4487A3ED876CE4BB95C5E4982E5EB64BA4FADE2E7F1125F80F910EB9BE78DB48CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20'
        ]

        when: println '(1) the transaction is fully processed'
        Message request = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(HexValue.toByteString(contractBinary)).setGas(HexValue.toByteString(12)).setGasPrice(HexValue.toByteString(223)).build()
        ).build()
        Message response = web3Service.ethSendTransaction(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        and: 'the callee address is remembered'
        request = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(((Types.ResponseEthSendTransaction) response).getHash()).build()
        response = web3Service.ethGetTransactionReceipt(request)
        callee.address = HexValue.toString(((Types.ResponseEthGetTransactionReceipt) response).getTxReceipt().contractAddress)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetTransactionReceipt
        ((Types.ResponseEthGetTransactionReceipt) response).getTxReceipt() != null

        when: println '(2) the newly created contract account is verified'
        request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(callee.address)).build()
        response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetBalance
        ((Types.ResponseEthGetBalance) response).getBalance() == HexValue.toByteString(0)

        when: println '(3) the storage of the contract is retrieved'
        request = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(callee.address)).build()
        response = web3Service.ethGetStorageAt(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetStorageAt
        HexValue.toBigInteger(((Types.ResponseEthGetStorageAt) response).getValue()) == BigInteger.valueOf(5)

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

        then: 'no events exist'
        response instanceof Types.ResponseEthGetFilterChanges
        ((Types.ResponseEthGetFilterChanges) response).getLogCount() == 0

        when: println '(6) the get contract method is executed'
        request = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(callee.address)).setData(HexValue.toByteString(getFunction.encode([]))).build()
        ).build()
        response = web3Service.ethCall(request as Types.RequestEthCall)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthCall
        HexValue.toBigInteger(((Types.ResponseEthCall) response).getReturn()) == BigInteger.valueOf(5)

        when: println '(7) the set contract method is executed'
        request = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(callee.address)).setData(HexValue.toByteString(setFunction.encode([BigInteger.valueOf(7)]))).setGas(HexValue.toByteString(12)).setGasPrice(HexValue.toByteString(223)).build()
        ).build()
        response = web3Service.ethSendTransaction(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthSendTransaction
        ((Types.ResponseEthSendTransaction) response).getHash() != null

        when: println '(8) the get contract method is executed again'
        request = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(callee.address)).setData(HexValue.toByteString(getFunction.encode([]))).build()
        ).build()
        response = web3Service.ethCall(request as Types.RequestEthCall)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthCall
        HexValue.toBigInteger(((Types.ResponseEthCall) response).getReturn()) == BigInteger.valueOf(7)

        when: println '(9) the storage of the contract is retrieved'
        request = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(callee.address)).build()
        response = web3Service.ethGetStorageAt(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetStorageAt
        HexValue.toBigInteger(((Types.ResponseEthGetStorageAt) response).getValue()) == BigInteger.valueOf(7)

        when: println '(10) we poll for events again'
        request = Types.RequestEthGetFilterChanges.newBuilder().setId(filterId).build()
        response = web3Service.ethGetFilterChanges(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetFilterChanges
        ((Types.ResponseEthGetFilterChanges) response).getLogCount() == 3
        ((Types.ResponseEthGetFilterChanges) response).getLog(0).address.size() == 32*2+2
        HexValue.decode(HexValue.toString(((Types.ResponseEthGetFilterChanges) response).getLog(0).data)) == '5'
        HexValue.decode(HexValue.toString(((Types.ResponseEthGetFilterChanges) response).getLog(1).data)) == '7'
        HexValue.decode(HexValue.toString(((Types.ResponseEthGetFilterChanges) response).getLog(2).data)) == '7'
    }
}