/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.service

import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.softwareag.tom.contract.ConfigLocationFileSystem
import com.softwareag.tom.contract.Contract
import com.softwareag.tom.contract.ContractRegistry
import com.softwareag.tom.contract.SolidityLocationFileSystem
import com.softwareag.tom.contract.abi.ContractInterface
import com.softwareag.tom.extension.Node
import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.ServiceHttp
import com.softwareag.tom.protocol.util.HexValue
import rx.Observable
import rx.Observer
import rx.Subscription
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * System under specification: {@link Web3Service}.
 * @author tglaeser
 */
class BurrowSpecification extends Specification {

    @Shared @Node protected ConfigObject config
    @Shared protected Web3Service web3Service

    def setup() {
        given: 'a JSON-RPC client'
        web3Service = Web3Service.build(new ServiceHttp("http://${config.node.host.ip}:${config.node.host.web3.port}"))
    }

    def "test 'web3ClientVersion' service"() {
        when: 'we make a get request'
        Types.RequestWeb3ClientVersion request = Types.RequestWeb3ClientVersion.newBuilder().build()
        Message response = web3Service.web3ClientVersion()
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseWeb3ClientVersion
        ((Types.ResponseWeb3ClientVersion)response).version.startsWith '0.29.1'
    }

    def "test 'netListening' service"() {
        when: 'we make a get request'
        Types.RequestNetListening request = Types.RequestNetListening.newBuilder().build()
        Message response = web3Service.netListening()
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseNetListening
        ((Types.ResponseNetListening) response).getListening()
    }

    def "test 'ethGetBalance' service"() {
        when: 'we make a get request'
        Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString("0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19")).build()
        Message response = web3Service.ethGetBalance(request)
        println ">>> $request.descriptorForType.fullName....$request<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthGetBalance
        HexValue.toBigInteger(((Types.ResponseEthGetBalance) response).getBalance() as ByteString) == 99999999999999 * Math.pow(10, 18) // 1 ETH = 10^18 Wei
    }

    def "test 'ethNewBlockFilter' service"() {
        when: 'we make a get request'
        Types.RequestEthNewBlockFilter request = Types.RequestEthNewBlockFilter.newBuilder().build()
        Message response = web3Service.ethNewBlockFilter()
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response instanceof Types.ResponseEthNewFilter
        ((Types.ResponseEthNewFilter) response).id.size() == 64 + 2
    }

    def "test create solidity contract and call event services"() {
        given: 'a valid Solidity contract'
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/util/Console']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification functionLog = functions.get(0)
        assert functionLog.name == 'log'
        List events = contract.abi.events as List<ContractInterface.Specification>
        ContractInterface.Specification eventLogAddress = events.get(0)
        assert eventLogAddress.name == 'LogAddress'

        String contractAddress

        when: println '(1) contract "sample/util/Console" gets deployed'
        Types.RequestEthSendTransaction requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(HexValue.toByteString(contract.binary)).setGas(HexValue.toByteString(contract.gasLimit)).setGasPrice(HexValue.toByteString(contract.gasPrice)).build()
        ).build()
        Types.ResponseEthSendTransaction responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction)
        println ">>> $requestEthSendTransaction.descriptorForType.fullName....$requestEthSendTransaction<<< $responseEthSendTransaction.descriptorForType.fullName...$responseEthSendTransaction"

        and: 'the contract address is remembered'
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().contractAddress)
        println ">>> $requestEthGetTransactionReceipt.descriptorForType.fullName....$requestEthGetTransactionReceipt<<< $responseEthGetTransactionReceipt.descriptorForType.fullName...$responseEthGetTransactionReceipt"

        then: 'a valid response is received'
        responseEthGetTransactionReceipt.getTxReceipt() != null

        when: println '(2) the newly created contract account is verified'
        Types.RequestEthGetBalance requestEthGetBalance = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        Types.ResponseEthGetBalance responseEthGetBalance = web3Service.ethGetBalance(requestEthGetBalance)
        println ">>> $requestEthGetBalance.descriptorForType.fullName....$requestEthGetBalance<<< $responseEthGetBalance.descriptorForType.fullName...$responseEthGetBalance"

        then: 'a valid response is received'
        responseEthGetBalance.getBalance() == HexValue.toByteString(0)

        when: println '(3) the storage of the contract is retrieved'
        Types.RequestEthGetStorageAt requestEthGetStorageAt = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        Types.ResponseEthGetStorageAt responseEthGetStorageAt = web3Service.ethGetStorageAt(requestEthGetStorageAt)
        println ">>> $requestEthGetStorageAt.descriptorForType.fullName....$requestEthGetStorageAt<<< $responseEthGetStorageAt.descriptorForType.fullName...$responseEthGetStorageAt"

        then: 'a valid response is received'
        responseEthGetStorageAt.getValue() != null

        when: println '(4) we subscribe to events from the the new contract account'
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        ).build()
        Types.ResponseEthNewFilter responseEthNewFilter = web3Service.ethNewFilter(requestEthNewFilter)
        println ">>> $requestEthNewFilter.descriptorForType.fullName....$requestEthNewFilter<<< $responseEthNewFilter.descriptorForType.fullName...$responseEthNewFilter"

        and: 'the filter id is remembered'
        def filterId = responseEthNewFilter.getId()

        then: 'a valid response is received'
        responseEthNewFilter.getId().size() == 32*2+2

        when: println '(5) we poll for events'
        Types.RequestEthGetFilterChanges requestEthGetFilterChanges = Types.RequestEthGetFilterChanges.newBuilder().setId(filterId).build()
        Types.ResponseEthGetFilterChanges responseEthGetFilterChanges = web3Service.ethGetFilterChanges(requestEthGetFilterChanges)
        println ">>> $requestEthGetFilterChanges.descriptorForType.fullName....$requestEthGetFilterChanges<<< $responseEthGetFilterChanges.descriptorForType.fullName...$responseEthGetFilterChanges\n"

        then: 'a valid response is received'
        responseEthGetFilterChanges.getEventCount() == 0

        when: println '(6) function "log" is executed 2 times'
        Types.RequestEthCall requestEthCall = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionLog.encode())).build()
        ).build()
        Types.ResponseEthCall responseEthCall = null
        2.times {
            responseEthCall = web3Service.ethCall(requestEthCall)
            println ">>> $requestEthCall.descriptorForType.fullName....$requestEthCall<<< $responseEthCall.descriptorForType.fullName...$responseEthCall"
        }

        then: 'a valid response is received'
        responseEthCall.return != null

        when: println '(7) we poll for events again'
        requestEthGetFilterChanges = Types.RequestEthGetFilterChanges.newBuilder().setId(filterId).build()
        responseEthGetFilterChanges = web3Service.ethGetFilterChanges(requestEthGetFilterChanges)
        println ">>> $requestEthGetFilterChanges.descriptorForType.fullName....$requestEthGetFilterChanges<<< $responseEthGetFilterChanges.descriptorForType.fullName...$responseEthGetFilterChanges"

        then: 'a valid response is received'
        responseEthGetFilterChanges.getEventCount() == 2
        responseEthGetFilterChanges.getEvent(1).getLog().address.size() == 32*2+2
        responseEthGetFilterChanges.getEvent(1).getLog().data.size() == 32*2+2
        responseEthGetFilterChanges.getEvent(1).getLog().getTopicCount() == 1
        HexValue.stripPrefix(HexValue.toString(responseEthGetFilterChanges.getEvent(1).getLog().getTopic(0))).equalsIgnoreCase(eventLogAddress.encode())
    }

    def "test create solidity contract and listen to events"() {
        given: 'a valid Solidity contract'
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/util/Console']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification functionLog = functions.get(0)
        assert functionLog.name == 'log'
        List events = contract.abi.events as List<ContractInterface.Specification>
        ContractInterface.Specification eventLogAddress = events.get(0)
        assert eventLogAddress.name == 'LogAddress'

        String contractAddress

        when: println '(1) contract "sample/util/Console" gets deployed'
        Types.RequestEthSendTransaction requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(HexValue.toByteString(contract.binary)).setGas(HexValue.toByteString(contract.gasLimit)).setGasPrice(HexValue.toByteString(contract.gasPrice)).build()
        ).build()
        Types.ResponseEthSendTransaction responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction)
        println ">>> $requestEthSendTransaction.descriptorForType.fullName....$requestEthSendTransaction<<< $responseEthSendTransaction.descriptorForType.fullName...$responseEthSendTransaction"

        and: 'the contract address is remembered'
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().contractAddress)
        println ">>> $requestEthGetTransactionReceipt.descriptorForType.fullName....$requestEthGetTransactionReceipt<<< $responseEthGetTransactionReceipt.descriptorForType.fullName...$responseEthGetTransactionReceipt"

        then: 'a valid response is received'
        responseEthGetTransactionReceipt.getTxReceipt() != null

        when: println '(2) we register for events with the new contract account'
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        ).build()
        Observable<Types.FilterLogType> ethLogObservable = web3Service.ethLogObservable(requestEthNewFilter)
        println ">>> $requestEthNewFilter.descriptorForType.fullName....$requestEthNewFilter<<< $ethLogObservable\n"

        and: 'subscribe to events'
        List<Types.FilterLogType> results = []
        CountDownLatch transactionLatch = new CountDownLatch(3)
        CountDownLatch completedLatch = new CountDownLatch(1)
        Subscription ethLogSubscription = ethLogObservable.subscribe([
                onCompleted: {
                    completedLatch.countDown()
                },
                onError    : { Throwable e ->
                    throw e
                },
                onNext     : { Types.FilterLogType result ->
                    results.add(result)
                    transactionLatch.countDown()
                }
        ] as Observer)

        then: 'the ReactiveX system gets properly initialized'
        ethLogObservable != null
        ethLogSubscription != null

        when: println '(3) we listen for events'
        println "results<<< $results\n"

        then: 'a valid response is received'
        results.size() == 0

        when: println '(4) function "log" is executed 3 times'
        Types.RequestEthCall requestEthCall = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionLog.encode())).build()
        ).build()
        Types.ResponseEthCall responseEthCall = null
        3.times {
            responseEthCall = web3Service.ethCall(requestEthCall)
            println ">>> $requestEthCall.descriptorForType.fullName....$requestEthCall<<< $responseEthCall.descriptorForType.fullName...$responseEthCall"
        }

        then: 'a valid response is received'
        responseEthCall.return != null

        when: println '(5) we wait a little while continuously listening for events'
        transactionLatch.await(15, TimeUnit.SECONDS)
        println "results<<< $results\n"

        then: 'a valid response is received'
        notThrown Throwable
        results.size() == 3
        results[1].address.size() == 32*2+2
        results[1].data.size() == 32*2+2
        results[1].topicCount == 1
        HexValue.stripPrefix(HexValue.toString(results[1].getTopic(0))).equalsIgnoreCase(eventLogAddress.encode())

        when: println '(6) the subscription is terminated'
        ethLogSubscription.unsubscribe()

        then: 'the subscriber has been removed'
        ethLogSubscription.isUnsubscribed()
    }

    def "test create solidity contract and store/update data"() {
        given: 'a valid Solidity contract'
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/SimpleStorage']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification functionSet = functions.get(1)
        assert functionSet.name == 'set'
        ContractInterface.Specification functionGet = functions.get(2)
        assert functionGet.name == 'get'
        List events = contract.abi.events as List<ContractInterface.Specification>
        ContractInterface.Specification eventLogUint = events.get(1)
        assert eventLogUint.name == 'LogUint'

        String contractAddress

        when: println '(1) contract "sample/SimpleStorage" gets deployed'
        Types.RequestEthSendTransaction requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setData(HexValue.toByteString(contract.binary)).setGas(HexValue.toByteString(contract.gasLimit)).setGasPrice(HexValue.toByteString(contract.gasPrice)).build()
        ).build()
        Types.ResponseEthSendTransaction responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction)
        println ">>> $requestEthSendTransaction.descriptorForType.fullName....$requestEthSendTransaction<<< $responseEthSendTransaction.descriptorForType.fullName...$responseEthSendTransaction"

        and: 'the contract address is remembered'
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().contractAddress)
        println ">>> $requestEthGetTransactionReceipt.descriptorForType.fullName....$requestEthGetTransactionReceipt<<< $responseEthGetTransactionReceipt.descriptorForType.fullName...$responseEthGetTransactionReceipt"

        then: 'a valid response is received'
        responseEthGetTransactionReceipt.getTxReceipt() != null

        when: println '(2) the newly created contract account is verified'
        Types.RequestEthGetBalance requestEthGetBalance = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        Types.ResponseEthGetBalance responseEthGetBalance = web3Service.ethGetBalance(requestEthGetBalance)
        println ">>> $requestEthGetBalance.descriptorForType.fullName....$requestEthGetBalance<<< $responseEthGetBalance.descriptorForType.fullName...$responseEthGetBalance"

        then: 'a valid response is received'
        responseEthGetBalance.getBalance() == HexValue.toByteString(0)

        when: println '(3) the storage of the contract is retrieved'
        Types.RequestEthGetStorageAt requestEthGetStorageAt = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        Types.ResponseEthGetStorageAt responseEthGetStorageAt = web3Service.ethGetStorageAt(requestEthGetStorageAt)
        println ">>> $requestEthGetStorageAt.descriptorForType.fullName....$requestEthGetStorageAt<<< $responseEthGetStorageAt.descriptorForType.fullName...$responseEthGetStorageAt"

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthGetStorageAt.getValue()) == BigInteger.valueOf(5)

        when: println '(4) we subscribe to events from the the new contract account'
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        ).build()
        Types.ResponseEthNewFilter responseEthNewFilter = web3Service.ethNewFilter(requestEthNewFilter)
        println ">>> $requestEthNewFilter.descriptorForType.fullName....$requestEthNewFilter<<< $responseEthNewFilter.descriptorForType.fullName...$responseEthNewFilter"

        and: 'the filter id is remembered'
        def filterId = responseEthNewFilter.getId()

        then: 'a valid response is received'
        responseEthNewFilter.getId().size() == 32*2+2

        when: println '(5) we poll for events'
        Types.RequestEthGetFilterChanges requestEthGetFilterChanges = Types.RequestEthGetFilterChanges.newBuilder().setId(filterId).build()
        Types.ResponseEthGetFilterChanges responseEthGetFilterChanges = web3Service.ethGetFilterChanges(requestEthGetFilterChanges)
        println ">>> $requestEthGetFilterChanges.descriptorForType.fullName....$requestEthGetFilterChanges<<< $responseEthGetFilterChanges.descriptorForType.fullName...$responseEthGetFilterChanges\n"

        then: 'no events exist'
        responseEthGetFilterChanges.getEventCount() == 0

        when: println '(6) function "get" is executed'
        Types.RequestEthCall requestEthCall = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionGet.encode())).build()
        ).build()
        Types.ResponseEthCall responseEthCall = web3Service.ethCall(requestEthCall)
        println ">>> $requestEthCall.descriptorForType.fullName....$requestEthCall<<< $responseEthCall.descriptorForType.fullName...$responseEthCall"

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthCall.getReturn()) == BigInteger.valueOf(5)

        when: println '(7) function "set" is executed'
        requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionSet.encode([BigInteger.valueOf(7)]))).setGas(HexValue.toByteString(12)).setGasPrice(HexValue.toByteString(223)).build()
        ).build()
        responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction)
        println ">>> $requestEthSendTransaction.descriptorForType.fullName....$requestEthSendTransaction<<< $responseEthSendTransaction.descriptorForType.fullName...$responseEthSendTransaction"

        then: 'a valid response is received'
        responseEthSendTransaction.getHash() != null

        when: println '(8) function "get" is executed again'
        requestEthCall = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionGet.encode())).build()
        ).build()
        responseEthCall = web3Service.ethCall(requestEthCall)
        println ">>> $requestEthCall.descriptorForType.fullName....$requestEthCall<<< $responseEthCall.descriptorForType.fullName...$responseEthCall"

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthCall.getReturn()) == BigInteger.valueOf(7)

        when: println '(9) the storage of the contract is retrieved'
        requestEthGetStorageAt = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        responseEthGetStorageAt = web3Service.ethGetStorageAt(requestEthGetStorageAt)
        println ">>> $requestEthGetStorageAt.descriptorForType.fullName....$requestEthGetStorageAt<<< $responseEthGetStorageAt.descriptorForType.fullName...$responseEthGetStorageAt"

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthGetStorageAt.getValue()) == BigInteger.valueOf(7)

        when: println '(10) we poll for events again'
        requestEthGetFilterChanges = Types.RequestEthGetFilterChanges.newBuilder().setId(filterId).build()
        responseEthGetFilterChanges = web3Service.ethGetFilterChanges(requestEthGetFilterChanges)
        println ">>> $requestEthGetFilterChanges.descriptorForType.fullName....$requestEthGetFilterChanges<<< $responseEthGetFilterChanges.descriptorForType.fullName...$responseEthGetFilterChanges"

        then: 'a valid response is received'
        responseEthGetFilterChanges.getEventCount() == 1
        responseEthGetFilterChanges.getEvent(0).getLog().address.size() == 32*2+2
        HexValue.decode(HexValue.toString(responseEthGetFilterChanges.getEvent(0).getLog().data)) == '7'
        responseEthGetFilterChanges.getEvent(0).getLog().getTopicCount() == 1
        HexValue.stripPrefix(HexValue.toString(responseEthGetFilterChanges.getEvent(0).getLog().getTopic(0))).equalsIgnoreCase(eventLogUint.encode())

        when: println '(11) we unsubscribe to events from the the new contract account'
        Types.RequestEthUninstallFilter requestEthUninstallFilter = Types.RequestEthUninstallFilter.newBuilder().setId(filterId).build()
        Types.ResponseEthUninstallFilter responseEthUninstallFilter = web3Service.ethUninstallFilter(requestEthUninstallFilter)
        println ">>> $requestEthUninstallFilter.descriptorForType.fullName....$requestEthUninstallFilter<<< $responseEthUninstallFilter.descriptorForType.fullName...$responseEthUninstallFilter"

        then: 'the filter was successfully removed'
        responseEthUninstallFilter.removed
    }
}