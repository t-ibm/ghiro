/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.service

import com.google.protobuf.ByteString
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
        ByteString callerByteString = ByteString.copyFrom(HexValue.toByteArray(caller))
        ByteString calleeByteString = ByteString.copyFrom(HexValue.toByteArray(callee))
        Payload.TxInput txInput = Payload.TxInput.newBuilder().setAddress(callerByteString).setAmount(20).build()
        Payload.TxOutput txOutput = Payload.TxOutput.newBuilder().setAddress(calleeByteString).setAmount(20).build()

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
        ByteString byteString = ByteString.copyFrom(HexValue.toByteArray(address))
        RpcQuery.GetAccountParam request = RpcQuery.GetAccountParam.newBuilder().setAddress(byteString).build()
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
}