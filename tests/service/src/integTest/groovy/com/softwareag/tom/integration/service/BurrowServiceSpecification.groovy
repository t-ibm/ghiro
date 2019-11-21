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
import com.softwareag.tom.protocol.api.BurrowQuery
import com.softwareag.tom.protocol.api.BurrowTransact
import com.softwareag.tom.protocol.grpc.ServiceQuery
import com.softwareag.tom.protocol.grpc.ServiceTransact
import com.softwareag.tom.protocol.util.HexValue
import io.grpc.stub.StreamObserver
import org.hyperledger.burrow.Acm
import org.hyperledger.burrow.execution.Exec
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

    def setup() {
        burrowQuery = BurrowService.query(new ServiceQuery(config.node.host.ip, config.node.host.grpc.port))
        burrowTransact = BurrowService.transact(new ServiceTransact(config.node.host.ip, config.node.host.grpc.port))
    }

    def cleanup() {
        burrowQuery.getService().shutdown()
        burrowTransact.getService().shutdown()
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
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        response.receipt.txType == 1
        HexValue.toString(response.receipt.contractAddress) == '\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000'
    }

    def "test 'burrow.query.GetAccount' service"() {
        when: 'we send a request'
        String address = "0x06dbb282d7d83654f19ee8fffc105a9e29b1d2f5"
        RpcQuery.GetAccountParam request = RpcQuery.GetAccountParam.newBuilder().setAddress(HexValue.copyFrom(address)).build()
        Acm.Account response = burrowQuery.getAccount(request)
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        HexValue.toString(response.getAddress().toByteArray()) == address
        response.getPermissions().getBase().getPerms() == 0
        response.getPermissions().getBase().getSetBit() == 0
    }

    def "test sync 'burrow.query.ListAccounts' service"() {
        when: 'we send a request'
        String query = 'Balance > 10'
        RpcQuery.ListAccountsParam request = RpcQuery.ListAccountsParam.newBuilder().setQuery(query).build()
        Iterator<Acm.Account> response = burrowQuery.listAccounts(request)
        println ">>> $request.descriptorForType.fullName....$request"
        List<Acm.Account> accounts = response.collect { println "<<< $it.descriptorForType.fullName...$it" }

        then: 'a valid response is received'
        accounts.size() == 4
    }

    def "test async 'burrow.query.ListAccounts' service"() {
        given: 'a stream observer'
        List<String> addresses = ['0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19', '0xf7f6574303cf1a29bb3402f21f60e130bc0b3fbe']
        List<Acm.Account> actual = []
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
                actual.add(result)
                stream.countDown()
            }
        ] as StreamObserver<Acm.Account>

        when: 'we send a request'
        String query = 'Balance > 1000000'
        RpcQuery.ListAccountsParam request = RpcQuery.ListAccountsParam.newBuilder().setQuery(query).build()
        burrowQuery.listAccounts(request, observer)
        println ">>> $request.descriptorForType.fullName....$request"

        and: 'the stream is received'
        stream.await(1, TimeUnit.SECONDS)
        done.await(1, TimeUnit.SECONDS)

        then: 'the stream match the expectation'
        actual.iterator().collect { println "<<< $it.descriptorForType.fullName...$it" }
        notThrown Throwable
        actual.eachWithIndex { account, index ->
            HexValue.toString(account.address) == addresses.get(index)
        }
    }

    def "test create solidity contract and call event services"() {
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
        println ">>> $requestCallTx.descriptorForType.fullName....$requestCallTx"
        Exec.TxExecution responseTxExecution = burrowTransact.callTx(requestCallTx)
        println "<<< $responseTxExecution.descriptorForType.fullName...$responseTxExecution"

        and: 'the contract address is remembered'
        contractAddress = HexValue.toString(responseTxExecution.receipt.contractAddress.toByteArray())

        then: 'a valid response is received'
        contractAddress.size() == 42
        responseTxExecution.result.gasUsed == 21

        when: println '(2) the newly created contract account is verified'
        RpcQuery.GetAccountParam requestGetAccountParam = RpcQuery.GetAccountParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        Acm.Account responseAccount = burrowQuery.getAccount(requestGetAccountParam)
        println ">>> $requestGetAccountParam.descriptorForType.fullName....$requestGetAccountParam"
        println "<<< $responseAccount.descriptorForType.fullName...$responseAccount"

        then: 'a valid response is received'
        responseAccount.getEVMCode() != null

        when: println '(3) the storage of the contract is retrieved'
        RpcQuery.GetStorageParam requestGetStorageParam = RpcQuery.GetStorageParam.newBuilder().setAddress(HexValue.copyFrom(contractAddress)).build()
        RpcQuery.StorageValue responseStorageValue = burrowQuery.getStorage()
        println ">>> $requestGetStorageParam.descriptorForType.fullName....$requestGetStorageParam"
        println "<<< $responseStorageValue.descriptorForType.fullName...$responseStorageValue"

        then: 'a valid response is received'
        HexValue.toString(responseStorageValue.getValue()) == ""
    }
}