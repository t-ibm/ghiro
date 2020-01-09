/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.service

import com.softwareag.tom.contract.ConfigLocationFileSystem
import com.softwareag.tom.contract.Contract
import com.softwareag.tom.contract.ContractRegistry
import com.softwareag.tom.contract.SolidityLocationFileSystem
import com.softwareag.tom.contract.abi.ContractInterface
import com.softwareag.tom.extension.Node
import com.softwareag.tom.protocol.BurrowService
import com.softwareag.tom.protocol.api.BurrowEvents
import com.softwareag.tom.protocol.api.BurrowQuery
import com.softwareag.tom.protocol.api.BurrowTransact
import com.softwareag.tom.protocol.grpc.ServiceEvents
import com.softwareag.tom.protocol.grpc.ServiceQuery
import com.softwareag.tom.protocol.grpc.ServiceTransact
import com.softwareag.tom.protocol.util.HexValue
import io.grpc.stub.StreamObserver
import org.hyperledger.burrow.Acm
import org.hyperledger.burrow.execution.Exec
import org.hyperledger.burrow.rpc.RpcEvents
import org.hyperledger.burrow.rpc.RpcQuery
import org.hyperledger.burrow.txs.Payload
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * System under specification: {@link BurrowService}.
 * @author tglaeser
 */
class BurrowServiceSpecification extends Specification {

    @Shared @Node protected ConfigObject config
    @Shared protected BurrowQuery burrowQuery
    @Shared protected BurrowTransact burrowTransact
    @Shared protected BurrowEvents burrowEvents

    def setup() {
        burrowQuery = BurrowService.query(new ServiceQuery(config.node.host.ip, config.node.host.grpc.port))
        burrowTransact = BurrowService.transact(new ServiceTransact(config.node.host.ip, config.node.host.grpc.port))
        burrowEvents = BurrowService.events(new ServiceEvents(config.node.host.ip, config.node.host.grpc.port))
    }

    def cleanup() {
        burrowQuery.getService().shutdown()
        burrowTransact.getService().shutdown()
        burrowEvents.getService().shutdown()
    }

    def "test 'burrow.transact.SendTxSync' service"() {
        given: 'a caller with Send and CreateAccount permission and the address for the account to be created'
        String caller = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        String callee = '0x06dbb282d7d83654f19ee8fffc105a9e29b1d2f5'
        Payload.TxInput txInput = Payload.TxInput.newBuilder().setAddress(HexValue.copyFrom(caller)).setAmount(20).build()
        Payload.TxOutput txOutput = Payload.TxOutput.newBuilder().setAddress(HexValue.copyFrom(callee)).setAmount(20).build()

        when: 'we send a request'
        Payload.SendTx request = Payload.SendTx.newBuilder().addInputs(txInput).addOutputs(txOutput).build()
        Exec.TxExecution response = burrowTransact.sendTx(request)

        then: 'a valid response is received'
        response.receipt.txType == 1
        HexValue.toString(response.receipt.contractAddress) == '\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000'
    }

    def "test 'burrow.query.GetAccount' service"() {
        when: 'we send a request'
        String address = "0x06dbb282d7d83654f19ee8fffc105a9e29b1d2f5"
        RpcQuery.GetAccountParam request = RpcQuery.GetAccountParam.newBuilder().setAddress(HexValue.copyFrom(address)).build()
        Acm.Account response = burrowQuery.getAccount(request)

        then: 'a valid response is received'
        HexValue.toString(response.getAddress().toByteArray()) == address
        response.getPermissions().getBase().getPerms() == 0
        response.getPermissions().getBase().getSetBit() == 0
        response.getBalance() >= 20
    }

    def "test sync 'burrow.query.ListAccounts' service"() {
        when: 'we send a request'
        String query = 'Balance > 10'
        RpcQuery.ListAccountsParam request = RpcQuery.ListAccountsParam.newBuilder().setQuery(query).build()
        Iterator<Acm.Account> response = burrowQuery.listAccounts(request)
        List<Acm.Account> results = response.collect()
        println "results <<< $results\n"

        then: 'a valid response is received'
        results.size() == 4
    }

    def "test async 'burrow.query.ListAccounts' service"() {
        given: 'a stream observer'
        List<String> addresses = ['0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19', '0xf7f6574303cf1a29bb3402f21f60e130bc0b3fbe']
        List<Acm.Account> results = []
        CountDownLatch stream = new CountDownLatch(addresses.size())
        CountDownLatch done = new CountDownLatch(1)
        StreamObserver<Acm.Account> observer = [
            onCompleted: {
                done.countDown()
            },
            onError    : { Throwable e ->
                throw e
            },
            onNext     : { Acm.Account result ->
                results.add(result)
                stream.countDown()
            }
        ] as StreamObserver<Acm.Account>

        when: 'we send a request'
        String query = 'Balance > 1000000'
        RpcQuery.ListAccountsParam request = RpcQuery.ListAccountsParam.newBuilder().setQuery(query).build()
        burrowQuery.listAccounts(request, observer)

        and: 'the stream is received'
        stream.await(1, TimeUnit.SECONDS)
        done.await(1, TimeUnit.SECONDS)
        println "results <<< $results\n"

        then: 'the stream match the expectation'
        notThrown Throwable
        results.eachWithIndex { account, index ->
            HexValue.toString(account.address) == addresses.get(index)
        }
    }

    def "test create solidity contract"() {
        given: 'a valid Solidity contract'
        Map contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/util/Console']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification functionLog = functions.get(0)
        assert functionLog.name == 'log'
        List events = contract.abi.events as List<ContractInterface.Specification>
        ContractInterface.Specification eventLogAddress = events.get(0)
        assert eventLogAddress.name == 'LogAddress'

        String contractAddress

        when: println '(1) contract "sample/util/Console" gets deployed'
        String caller = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        Payload.TxInput txInput = Payload.TxInput.newBuilder().setAddress(HexValue.copyFrom(caller)).setAmount(20).build()
        Payload.CallTx requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(contract.binary)).build()
        Exec.TxExecution responseTxExecution = burrowTransact.callTx(requestCallTx)

        and: 'the contract address is remembered'
        contractAddress = HexValue.toString(responseTxExecution.receipt.contractAddress.toByteArray())

        then: 'a valid response is received'
        contractAddress.size() == 42
        responseTxExecution.result.gasUsed == 24

        when: println '(2) the newly created contract account is verified'
        RpcQuery.GetAccountParam requestGetAccountParam = RpcQuery.GetAccountParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        Acm.Account responseAccount = burrowQuery.getAccount(requestGetAccountParam)

        then: 'a valid response is received'
        responseAccount.getEVMCode() != null //TODO :: Compare value instead

        when: println '(3) the storage of the contract is retrieved'
        RpcQuery.GetStorageParam requestGetStorageParam = RpcQuery.GetStorageParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        RpcQuery.StorageValue responseStorageValue = burrowQuery.getStorage(requestGetStorageParam)

        then: 'a valid response is received'
        responseStorageValue.getValue().size() == 0 //TODO
    }

    def "test create solidity contract and listen to events"() {
        given: 'a valid Solidity contract'
        Map contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/util/Console']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification functionLog = functions.get(0)
        assert functionLog.name == 'log'
        List events = contract.abi.events as List<ContractInterface.Specification>
        ContractInterface.Specification eventLogAddress = events.get(0)
        assert eventLogAddress.name == 'LogAddress'

        String contractAddress

        when: println '(1) contract "sample/util/Console" gets deployed'
        String caller = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        Payload.TxInput txInput = Payload.TxInput.newBuilder().setAddress(HexValue.copyFrom(caller)).setAmount(20).build()
        Payload.CallTx requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(contract.binary)).build()
        Exec.TxExecution responseTxExecution = burrowTransact.callTx(requestCallTx)

        and: 'the contract address is remembered'
        contractAddress = HexValue.toString(responseTxExecution.receipt.contractAddress.toByteArray())

        then: 'a valid response is received'
        contractAddress.size() == 42
        responseTxExecution.result.gasUsed == 24

        when: println '(2) we register for events with the new contract account'
        List<RpcEvents.EventsResponse> results = []
        CountDownLatch stream = new CountDownLatch(3)
        CountDownLatch done = new CountDownLatch(1)
        StreamObserver<RpcEvents.EventsResponse> observer = [
            onCompleted: {
                done.countDown()
            },
            onError    : { Throwable e ->
                throw e
            },
            onNext     : { RpcEvents.EventsResponse eventsResponse ->
                results.add(eventsResponse)
                stream.countDown()
            }
        ] as StreamObserver<RpcEvents.EventsResponse>

        and: 'subscribe to events'
        String contractAddressUpperCase = HexValue.stripPrefix(contractAddress).toUpperCase() //TODO :: Fix contract address
        String query = "EventType = 'LogEvent' AND Address = '$contractAddressUpperCase'"
        RpcEvents.BlocksRequest request = RpcEvents.BlocksRequest.newBuilder().setBlockRange(
            RpcEvents.BlockRange.newBuilder()
                .setStart(RpcEvents.Bound.newBuilder().setType(RpcEvents.Bound.BoundType.LATEST))
                .setEnd(RpcEvents.Bound.newBuilder().setType(RpcEvents.Bound.BoundType.STREAM))
        ).setQuery(query).build()
        burrowEvents.getEvents(request, observer)

        then: 'the event system gets properly initialized'
        stream != null
        done != null

        when: println '(3) we listen for events'
        stream.await(1, TimeUnit.SECONDS)
        done.await(1, TimeUnit.SECONDS)
        println "results <<< $results\n"

        then: 'a valid response is received'
        results.size() == 0

        when: println '(4) function "log" is executed 3 times'
        requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setAddress(HexValue.copyFrom(contractAddress)).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(functionLog.encode())).build()
        3.times {
            responseTxExecution = burrowTransact.callTx(requestCallTx)
        }

        then: 'a valid response is received'
        responseTxExecution.result.gasUsed == 82

        when: println '(5) we wait a little while continuously listening for events'
        stream.await(15, TimeUnit.SECONDS)
        done.await(1, TimeUnit.SECONDS)
        println "results <<< $results\n"

        then: 'a valid response is received'
        notThrown Throwable
        results.size() == 3
        HexValue.toString(results[1].getEvents(0).log.address.toByteArray()) == contractAddress
        results[1].getEvents(0).log.topicsCount == 1
        HexValue.stripPrefix(HexValue.toString(results[1].getEvents(0).log.getTopics(0).toByteArray())).equalsIgnoreCase(eventLogAddress.encode()) //TODO :: Strip prefix
    }

    def "test create solidity contract and store/update data"() {
        given: 'a valid Solidity contract'
        Map contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
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
        String caller = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        Payload.TxInput txInput = Payload.TxInput.newBuilder().setAddress(HexValue.copyFrom(caller)).setAmount(20).build()
        Payload.CallTx requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(contract.binary)).build()
        Exec.TxExecution responseTxExecution = burrowTransact.callTx(requestCallTx)

        and: 'the contract address is remembered'
        contractAddress = HexValue.toString(responseTxExecution.receipt.contractAddress.toByteArray())

        then: 'a valid response is received'
        contractAddress.size() == 42
        responseTxExecution.result.gasUsed == 33

        when: println '(2) the newly created contract account is verified'
        RpcQuery.GetAccountParam requestGetAccountParam = RpcQuery.GetAccountParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        Acm.Account responseAccount = burrowQuery.getAccount(requestGetAccountParam)

        then: 'a valid response is received'
        responseAccount.getEVMCode() != null

        when: println '(3) the storage of the contract is retrieved'
        RpcQuery.GetStorageParam requestGetStorageParam = RpcQuery.GetStorageParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        RpcQuery.StorageValue responseStorageValue = burrowQuery.getStorage(requestGetStorageParam)

        then: 'a valid response is received'
        HexValue.asBigInteger(responseStorageValue.value) == 5

        when: println '(4) function "get" is executed'
        requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setAddress(HexValue.copyFrom(contractAddress)).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(functionGet.encode())).build()
        responseTxExecution = burrowTransact.callTx(requestCallTx)

        then: 'a valid response is received'
        HexValue.asBigInteger(responseTxExecution.result.return) == BigInteger.valueOf(5)

        when: println '(5) function "set" is executed'
        requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setAddress(HexValue.copyFrom(contractAddress)).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(functionSet.encode([BigInteger.valueOf(7)]))).build()
        responseTxExecution = burrowTransact.callTx(requestCallTx)

        then: 'a valid response is received'
        responseTxExecution.result.gasUsed == 257

        when: println '(6) function "get" is executed again'
        requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setAddress(HexValue.copyFrom(contractAddress)).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(functionGet.encode())).build()
        responseTxExecution = burrowTransact.callTx(requestCallTx)

        then: 'a valid response is received'
        HexValue.asBigInteger(responseTxExecution.result.return) == BigInteger.valueOf(7)

        when: println '(7) the storage of the contract is retrieved'
        requestGetStorageParam = RpcQuery.GetStorageParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        responseStorageValue = burrowQuery.getStorage(requestGetStorageParam)

        then: 'a valid response is received'
        HexValue.asBigInteger(responseStorageValue.value) == 7
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
        String caller = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        Payload.TxInput txInput = Payload.TxInput.newBuilder().setAddress(HexValue.copyFrom(caller)).setAmount(20).build()
        Payload.CallTx requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(contract.binary)).build()
        Exec.TxExecution responseTxExecution = burrowTransact.callTx(requestCallTx)

        and: 'the contract address is remembered'
        contractAddress = HexValue.toString(responseTxExecution.receipt.contractAddress.toByteArray())

        then: 'a valid response is received'
        contractAddress.size() == 42
        responseTxExecution.result.gasUsed == 709

        when: println '(2) we register for events with the new contract account'
        List<RpcEvents.EventsResponse> results = []
        CountDownLatch stream = new CountDownLatch(3)
        CountDownLatch done = new CountDownLatch(1)
        StreamObserver<RpcEvents.EventsResponse> observer = [
            onCompleted: {
                done.countDown()
            },
            onError    : { Throwable e ->
                throw e
            },
            onNext     : { RpcEvents.EventsResponse eventsResponse ->
                results.add(eventsResponse)
                stream.countDown()
            }
        ] as StreamObserver<RpcEvents.EventsResponse>

        and: 'subscribe to events'
        String contractAddressUpperCase = HexValue.stripPrefix(contractAddress).toUpperCase() //TODO :: Fix contract address
        String query = "EventType = 'LogEvent' AND Address = '$contractAddressUpperCase'"
        RpcEvents.BlocksRequest request = RpcEvents.BlocksRequest.newBuilder().setBlockRange(
            RpcEvents.BlockRange.newBuilder()
                .setStart(RpcEvents.Bound.newBuilder().setType(RpcEvents.Bound.BoundType.LATEST))
                .setEnd(RpcEvents.Bound.newBuilder().setType(RpcEvents.Bound.BoundType.STREAM))
        ).setQuery(query).build()
        burrowEvents.getEvents(request, observer)

        then: 'the event system gets properly initialized'
        stream != null
        done != null

        when: println '(3) we listen for events'
        stream.await(1, TimeUnit.SECONDS)
        done.await(1, TimeUnit.SECONDS)
        println "results <<< $results\n"

        then: 'a valid response is received'
        results.size() == 1

        when: println '(4) the newly created contract account is verified'
        RpcQuery.GetAccountParam requestGetAccountParam = RpcQuery.GetAccountParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        Acm.Account responseAccount = burrowQuery.getAccount(requestGetAccountParam)

        then: 'a valid response is received'
        responseAccount.getEVMCode() != null

        when: println '(5) the storage of the contract is retrieved'
        RpcQuery.GetStorageParam requestGetStorageParam = RpcQuery.GetStorageParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        RpcQuery.StorageValue responseStorageValue = burrowQuery.getStorage(requestGetStorageParam)

        then: 'a valid response is received'
        HexValue.asBigInteger(responseStorageValue.value) == 0 //TODO :: Seems to be right, but how can I retrieve the initial balance

        when: println '(6) function "approve" is executed'
        String spender = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        String data = functionApprove.encode([HexValue.toBigInteger(spender), BigInteger.valueOf(42)])
        requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setAddress(HexValue.copyFrom(contractAddress)).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(data)).build()
        responseTxExecution = burrowTransact.callTx(requestCallTx)

        then: 'a valid response is received'
        responseTxExecution.result.gasUsed == 338
        HexValue.asBigInteger(responseTxExecution.result.return) == BigInteger.valueOf(1)

        when: println '(7) function "transfer" is executed'
        requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setAddress(HexValue.copyFrom(contractAddress)).setGasLimit(contract.gasLimit.longValue()).setGasPrice(contract.gasPrice.longValue()).setFee(20).setData(HexValue.copyFrom(data)).build()
        responseTxExecution = burrowTransact.callTx(requestCallTx)

        then: 'a valid response is received'
        responseTxExecution.result.gasUsed == 338
        HexValue.asBigInteger(responseTxExecution.result.return) == BigInteger.valueOf(1)

        when: println '(8) we wait a little while continuously listening for events'
        stream.await(15, TimeUnit.SECONDS)
        done.await(1, TimeUnit.SECONDS)
        println "results <<< $results\n"

        then: 'a valid response is received'
        notThrown Throwable
        results.size() == 3
        results.each { it.getEvents(0).log.topicsCount == 3 }
        // Deploy
        HexValue.toString(results[0].getEvents(0).log.address.toByteArray()) == contractAddress
        HexValue.asBigInteger(results[0].getEvents(0).log.data) == BigInteger.valueOf(10).pow(22)
        HexValue.toString(results[0].getEvents(0).log.getTopics(1).toByteArray()) == '0x0000000000000000000000000000000000000000000000000000000000000000'
        HexValue.toString(results[0].getEvents(0).log.getTopics(2).toByteArray()) == '0x000000000000000000000000' + HexValue.stripPrefix(caller)
        // Approve
        HexValue.toString(results[1].getEvents(0).log.address.toByteArray()) == contractAddress
        HexValue.asBigInteger(results[1].getEvents(0).log.data) == BigInteger.valueOf(42)
        HexValue.toString(results[1].getEvents(0).log.getTopics(1).toByteArray()) == '0x000000000000000000000000' + HexValue.stripPrefix(caller)
        HexValue.toString(results[1].getEvents(0).log.getTopics(2).toByteArray()) == '0x000000000000000000000000' + HexValue.stripPrefix(caller)
        // Transfer
        HexValue.toString(results[2].getEvents(0).log.address.toByteArray()) == contractAddress
        HexValue.asBigInteger(results[2].getEvents(0).log.data) == BigInteger.valueOf(42)
        HexValue.toString(results[2].getEvents(0).log.getTopics(1).toByteArray()) == '0x000000000000000000000000' + HexValue.stripPrefix(caller)
        HexValue.toString(results[2].getEvents(0).log.getTopics(2).toByteArray()) == '0x000000000000000000000000' + HexValue.stripPrefix(caller)
    }
}