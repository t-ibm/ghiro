/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp

import com.softwareag.tom.protocol.Web3Service
import com.softwareag.tom.protocol.api.BurrowEvents
import com.softwareag.tom.protocol.api.BurrowQuery
import com.softwareag.tom.protocol.api.BurrowTransact
import com.softwareag.tom.protocol.jsonrpc.Request
import com.softwareag.tom.protocol.jsonrpc.Response
import com.softwareag.tom.protocol.jsonrpc.ResponseMock
import com.softwareag.tom.protocol.jsonrpc.Service
import com.softwareag.tom.protocol.util.HexValue
import io.grpc.stub.StreamObserver
import org.hyperledger.burrow.Acm
import org.hyperledger.burrow.execution.Exec
import org.hyperledger.burrow.rpc.RpcEvents
import org.hyperledger.burrow.rpc.RpcQuery
import org.hyperledger.burrow.txs.Payload
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Executors

class UtilBaseSpecification extends Specification {

    @Shared ResponseMock responseMock = new ResponseMock()
    @Shared Util util = Util.instance()

    def setupSpec() {
        setupJsonRpc()
        setupGRpc()
    }

    def setupJsonRpc() {
        // JSON-RPC
        Service service = Mock(Service)
        service.send(_ as Request, _ as Class) >> { Request request, Class c ->
            println ">>> $request"
            Response response = responseMock.getResponse(request)
            println "<<< $response"
            response
        }
        util.web3 = new ServiceSupplierWeb3(util, Web3Service.build(service, 1000, Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())))
    }

    def setupGRpc() {
        // gRPC
        BurrowQuery burrowQuery = Mock(BurrowQuery)
        burrowQuery.getAccount(_ as RpcQuery.GetAccountParam) >> { RpcQuery.GetAccountParam request ->
            println ">>> $request"
            Acm.Account response = Acm.Account.newBuilder().setEVMCode(HexValue.toByteString('0x0000')).build() //TODO
            println "<<< $response"
            response
        }
        BurrowTransact burrowTransact = Mock(BurrowTransact)
        burrowTransact.callTx(_ as Payload.CallTx) >> { Payload.CallTx request ->
            println ">>> $request"
            Exec.Result result = Exec.Result.newBuilder().setReturn(HexValue.copyFrom('')).build()
            Exec.TxExecution response = Exec.TxExecution.newBuilder().setResult(result).build()
            println "<<< $response"
            response
        }
        BurrowEvents burrowEvents = Mock(BurrowEvents)
        burrowEvents.getEvents(_ as RpcEvents.BlocksRequest, _ as StreamObserver<RpcEvents.EventsResponse>) >> { RpcEvents.BlocksRequest request, StreamObserver<RpcEvents.EventsResponse> observer ->
            println ">>> $request"
            long height = 108
            3.times {
                RpcEvents.EventsResponse value = RpcEvents.EventsResponse.newBuilder().setHeight(height).addEvents(
                    Exec.Event.newBuilder().setHeader(
                        Exec.Header.newBuilder().setTxType(2).setTxHash(HexValue.copyFrom('0xa8af028a6aa5a15ffbc6bd80795e0731f7f0b4f2777b4ea006ed97e878e1aaec')).setEventType(2).setEventID("Log/32B11B5AE572F59C0345223EC2403B7A91FD2DA2").setHeight(height).build()
                    ).setLog(
                        Exec.LogEvent.newBuilder().setAddress(HexValue.copyFrom('0x32B11B5AE572F59C0345223EC2403B7A91FD2DA2')).setData(HexValue.copyFrom('0x0000000000000000000000000c1bf03b1b90ac16a60349495b907eb5ff213507')).addTopics(HexValue.copyFrom('0xb123f68b8ba02b447d91a6629e121111b7dd6061ff418a60139c8bf00522a284')).build()
                    ).build()
                ).build()
                println "<<< $value"
                observer.onNext(value)
                height++
            }
        }
        util.burrow = new ServiceSupplierBurrow(util, burrowQuery, burrowTransact, burrowEvents)
    }
}