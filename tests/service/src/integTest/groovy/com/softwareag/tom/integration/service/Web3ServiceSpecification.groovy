/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.service

import com.google.protobuf.ByteString
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
import rx.exceptions.OnErrorFailedException
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * System under specification: {@link Web3Service}.
 * @author tglaeser
 */
class Web3ServiceSpecification extends Specification {

    @Shared @Node protected ConfigObject config
    @Shared protected Web3Service web3Service

    def setup() {
        given: 'a JSON-RPC client'
        web3Service = Web3Service.build(new ServiceHttp("http://${config.node.host.ip}:${config.node.host.web3.port}"))
    }

    def "test 'web3ClientVersion' service"() {
        when: 'we make a get request'
        Types.ResponseWeb3ClientVersion response = web3Service.web3ClientVersion()

        then: 'a valid response is received'
        response.version.startsWith '0.29.1'
    }

    def "test 'netListening' service"() {
        when: 'we make a get request'
        Types.ResponseNetListening response = web3Service.netListening()

        then: 'a valid response is received'
        response.listening
    }

    def "test 'ethGetBalance' service"() {
        when: 'we make a get request'
        Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString("0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19")).build()
        Types.ResponseEthGetBalance response = web3Service.ethGetBalance(request)

        then: 'a valid response is received'
        HexValue.toBigInteger(response.balance as ByteString) <= 99999999999999 * Math.pow(10, 18) // 1 ETH = 10^18 Wei
        HexValue.toBigInteger(response.balance as ByteString) >= 9999999999999 * Math.pow(10, 18) // 1 ETH = 10^18 Wei
    }

    def "test send payment"() {
        given: 'a caller with Send permission and the address for the account to be created'
        String caller = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        String callee = '0x06dbb282d7d83654f19ee8fffc105a9e29b1d2f5'

        when: 'we make a send transaction request'
        Types.RequestEthSendTransaction requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
            Types.TxType.newBuilder().setFrom(HexValue.toByteString(caller)).setTo(HexValue.toByteString(callee)).setValue(HexValue.toByteString(20)).build()
        ).build()
        Types.ResponseEthSendTransaction responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction)

        and: 'the transaction receipt gets requested'
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)

        then: 'a valid response is received'
        responseEthGetTransactionReceipt.getTxReceipt() != null

        when: 'we check for the new balance'
        Types.RequestEthGetBalance requestEthGetBalance = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(callee)).build()
        Types.ResponseEthGetBalance responseEthGetBalance = web3Service.ethGetBalance(requestEthGetBalance)

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthGetBalance.getBalance() as ByteString) >= 14 * Math.pow(10, 18) //TODO :: Should be 20 instead, not 14
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

        and: 'the contract address is remembered'
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().contractAddress)

        then: 'a valid response is received'
        responseEthGetTransactionReceipt.getTxReceipt() != null

        when: println '(2) the newly created contract account is verified'
        Types.RequestEthGetBalance requestEthGetBalance = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        Types.ResponseEthGetBalance responseEthGetBalance = web3Service.ethGetBalance(requestEthGetBalance)

        then: 'a valid response is received'
        responseEthGetBalance.getBalance() == HexValue.toByteString(0)

        when: println '(3) the storage of the contract is retrieved'
        Types.RequestEthGetStorageAt requestEthGetStorageAt = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        web3Service.ethGetStorageAt(requestEthGetStorageAt)

        then: 'a valid response is received'
        UnsupportedOperationException exception = thrown()
        exception.message == 'Error 3: not found'

        when: println '(4) we subscribe to events from the the new contract account'
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        ).build()
        web3Service.ethNewFilter(requestEthNewFilter)

        then: 'a valid response is received'
        exception = thrown()
        exception.message == 'Error 3: not found'

        when: println '(5) we poll for events'
        Types.RequestEthGetFilterChanges requestEthGetFilterChanges = Types.RequestEthGetFilterChanges.newBuilder().setId(ByteString.EMPTY).build()
        web3Service.ethGetFilterChanges(requestEthGetFilterChanges)

        then: 'a valid response is received'
        exception = thrown()
        exception.message == 'Error 3: not found'

        when: println '(6) function "log" is executed 2 times'
        Types.RequestEthCall requestEthCall = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionLog.encode())).build()
        ).build()
        Types.ResponseEthCall responseEthCall = null
        2.times {
            responseEthCall = web3Service.ethCall(requestEthCall)
        }

        then: 'a valid response is received'
        responseEthCall.return != null

        when: println '(7) we poll for events again'
        requestEthGetFilterChanges = Types.RequestEthGetFilterChanges.newBuilder().setId(ByteString.EMPTY).build()
        web3Service.ethGetFilterChanges(requestEthGetFilterChanges)

        then: 'a valid response is received'
        exception = thrown()
        exception.message == 'Error 3: not found'
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

        and: 'the contract address is remembered'
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().contractAddress)

        then: 'a valid response is received'
        responseEthGetTransactionReceipt.getTxReceipt() != null

        when: println '(2) we register for events with the new contract account'
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        ).build()
        Observable<Types.FilterLogType> ethLogObservable = web3Service.ethLogObservable(requestEthNewFilter)

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
        ethLogSubscription == null
        OnErrorFailedException exception = thrown()
        exception.message == 'Error occurred when trying to propagate error to Observer.onError'

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
        }

        then: 'a valid response is received'
        responseEthCall.return != null

        when: println '(5) we wait a little while continuously listening for events'
        transactionLatch.await(1, TimeUnit.SECONDS)
        println "results<<< $results\n"

        then: 'a valid response is received'
        notThrown Throwable
        results.size() == 0
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

        and: 'the contract address is remembered'
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().contractAddress)

        then: 'a valid response is received'
        responseEthGetTransactionReceipt.getTxReceipt() != null

        when: println '(2) the newly created contract account is verified'
        Types.RequestEthGetBalance requestEthGetBalance = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        Types.ResponseEthGetBalance responseEthGetBalance = web3Service.ethGetBalance(requestEthGetBalance)

        then: 'a valid response is received'
        responseEthGetBalance.getBalance() == HexValue.toByteString(0)

        when: println '(3) the storage of the contract is retrieved'
        Types.RequestEthGetStorageAt requestEthGetStorageAt = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        web3Service.ethGetStorageAt(requestEthGetStorageAt)

        then: 'a valid response is received'
        UnsupportedOperationException exception = thrown()
        exception.message == 'Error 3: not found'

        when: println '(4) we subscribe to events from the the new contract account'
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
                Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        ).build()
        web3Service.ethNewFilter(requestEthNewFilter)

        then: 'a valid response is received'
        exception = thrown()
        exception.message == 'Error 3: not found'

        when: println '(5) we poll for events'
        Types.RequestEthGetFilterChanges requestEthGetFilterChanges = Types.RequestEthGetFilterChanges.newBuilder().setId(ByteString.EMPTY).build()
        web3Service.ethGetFilterChanges(requestEthGetFilterChanges)

        then: 'no events exist'
        exception = thrown()
        exception.message == 'Error 3: not found'

        when: println '(6) function "get" is executed'
        Types.RequestEthCall requestEthCall = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionGet.encode())).build()
        ).build()
        Types.ResponseEthCall responseEthCall = web3Service.ethCall(requestEthCall)

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthCall.getReturn()) == BigInteger.valueOf(5)

        when: println '(7) function "set" is executed'
        requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionSet.encode([BigInteger.valueOf(7)]))).setGas(HexValue.toByteString(contract.gasLimit)).setGasPrice(HexValue.toByteString(contract.gasPrice)).build()
        ).build()
        responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction)

        then: 'a valid response is received'
        responseEthSendTransaction.getHash() != null

        when: println '(8) function "get" is executed again'
        requestEthCall = Types.RequestEthCall.newBuilder().setTx(
                Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(functionGet.encode())).build()
        ).build()
        responseEthCall = web3Service.ethCall(requestEthCall)

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthCall.getReturn()) == BigInteger.valueOf(7)

        when: println '(9) the storage of the contract is retrieved'
        requestEthGetStorageAt = Types.RequestEthGetStorageAt.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        web3Service.ethGetStorageAt(requestEthGetStorageAt)

        then: 'a valid response is received'
        exception = thrown()
        exception.message == 'Error 3: not found'

        when: println '(10) we poll for events again'
        requestEthGetFilterChanges = Types.RequestEthGetFilterChanges.newBuilder().setId(ByteString.EMPTY).build()
        web3Service.ethGetFilterChanges(requestEthGetFilterChanges)

        then: 'a valid response is received'
        exception = thrown()
        exception.message == 'Error 3: not found'

        when: println '(11) we unsubscribe to events from the the new contract account'
        Types.RequestEthUninstallFilter requestEthUninstallFilter = Types.RequestEthUninstallFilter.newBuilder().setId(ByteString.EMPTY).build()
        web3Service.ethUninstallFilter(requestEthUninstallFilter)

        then: 'the filter was successfully removed'
        exception = thrown()
        exception.message == 'Error 3: not found'
    }

    def "test create solidity contract and exchange token"() {
        given: 'a valid Solidity contract'
        Map contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['zeppelin/examples/SimpleToken']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification functionApprove = functions.get(1)
        assert functionApprove.name == 'approve'
        ContractInterface.Specification functionTransfer = functions.get(9)
        assert functionTransfer.name == 'transfer'
        List events = contract.abi.events as List<ContractInterface.Specification>
        ContractInterface.Specification eventTransfer = events.get(0)
        assert eventTransfer.name == 'Transfer'
        ContractInterface.Specification eventApproval = events.get(1)
        assert eventApproval.name == 'Approval'

        String contractAddress

        when: println '(1) contract "zeppelin/examples/SimpleToken" gets deployed'
        Types.RequestEthSendTransaction requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
            Types.TxType.newBuilder().setData(HexValue.toByteString(contract.binary)).setGas(HexValue.toByteString(contract.gasLimit)).setGasPrice(HexValue.toByteString(contract.gasPrice)).build()
        ).build()
        Types.ResponseEthSendTransaction responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction)

        and: 'the contract address is remembered'
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().contractAddress)

        then: 'a valid response is received'
        responseEthGetTransactionReceipt.getTxReceipt() != null

        when: println '(2) the newly created contract account is verified'
        Types.RequestEthGetBalance requestEthGetBalance = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(contractAddress)).build()
        Types.ResponseEthGetBalance responseEthGetBalance = web3Service.ethGetBalance(requestEthGetBalance)

        then: 'a valid response is received'
        responseEthGetBalance.getBalance() == HexValue.toByteString(0)

        when: println '(3) function "approve" is executed'
        String spender = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        String data = functionApprove.encode([HexValue.toBigInteger(spender), BigInteger.valueOf(42)])
        Types.RequestEthCall requestEthCall = Types.RequestEthCall.newBuilder().setTx(
            Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(data)).build()
        ).build()
        Types.ResponseEthCall responseEthCall = web3Service.ethCall(requestEthCall)

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthCall.getReturn()) == BigInteger.valueOf(1)

        when: println '(4) function "transfer" is executed'
        requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
            Types.TxType.newBuilder().setTo(HexValue.toByteString(contractAddress)).setData(HexValue.toByteString(data)).setGas(HexValue.toByteString(contract.gasLimit)).setGasPrice(HexValue.toByteString(contract.gasPrice)).build()
        ).build()
        responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction)

        and: 'the transaction receipt gets requested'
        requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build()
        responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt)

        then: 'a valid response is received'
        HexValue.toBigInteger(responseEthGetTransactionReceipt.getTxReceipt().gasUsed) == 338
    }
}