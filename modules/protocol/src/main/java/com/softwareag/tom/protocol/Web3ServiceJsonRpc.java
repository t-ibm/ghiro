/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.JsonRpcRx;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthCall;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetBalance;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetFilterChanges;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetStorageAt;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetTransactionReceipt;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewBlockFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthSendTransaction;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthUninstallFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestNetListening;
import com.softwareag.tom.protocol.jsonrpc.request.RequestWeb3ClientVersion;
import rx.Observable;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Web3 over JSON-RPC service implementation.
 */
class Web3ServiceJsonRpc extends MessageBase implements Web3Service {
    private static final int BLOCK_TIME = 10 * 1000;

    private final Service jsonRpcService;
    private final JsonRpcRx jsonRpcRx;
    private final long blockTime;

    Web3ServiceJsonRpc(Service jsonRpcService) {
        this(jsonRpcService, BLOCK_TIME, Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    Web3ServiceJsonRpc(Service jsonRpcService, long pollingInterval, ScheduledExecutorService scheduledExecutorService) {
        this.jsonRpcService = jsonRpcService;
        this.jsonRpcRx = new JsonRpcRx(jsonRpcService, scheduledExecutorService);
        this.blockTime = pollingInterval;
    }

    @Override public Types.ResponseWeb3ClientVersion web3ClientVersion() throws IOException {
        RequestWeb3ClientVersion jsonRpcRequest = new RequestWeb3ClientVersion(jsonRpcService) {};
        Types.ResponseWeb3ClientVersion response = jsonRpcRequest.send().getResponse();
        log(Types.RequestWeb3ClientVersion.newBuilder().build(), response);
        return response;
    }

    @Override public Types.ResponseNetListening netListening() throws IOException {
        RequestNetListening jsonRpcRequest = new RequestNetListening(jsonRpcService) {};
        Types.ResponseNetListening response = jsonRpcRequest.send().getResponse();
        log(Types.RequestNetListening.newBuilder().build(), response);
        return response;
    }

    @Override public Types.ResponseEthGetBalance ethGetBalance(Types.RequestEthGetBalance request) throws IOException {
        RequestEthGetBalance jsonRpcRequest = new RequestEthGetBalance(jsonRpcService, request) {};
        Types.ResponseEthGetBalance response = jsonRpcRequest.send().getResponse();
        log(request, response);
        return response;
    }

    @Override public Types.ResponseEthGetStorageAt ethGetStorageAt(Types.RequestEthGetStorageAt request) throws IOException {
        RequestEthGetStorageAt jsonRpcRequest = new RequestEthGetStorageAt(jsonRpcService, request) {};
        Types.ResponseEthGetStorageAt response = jsonRpcRequest.send().getResponse();
        log(request, response);
        return response;
    }

    @Override public Types.ResponseEthSendTransaction ethSendTransaction(Types.RequestEthSendTransaction request) throws IOException {
        RequestEthSendTransaction jsonRpcRequest = new RequestEthSendTransaction(jsonRpcService, request) {};
        Types.ResponseEthSendTransaction response = jsonRpcRequest.send().getResponse();
        log(request, response);
        return response;
    }

    @Override public Types.ResponseEthCall ethCall(Types.RequestEthCall request) throws IOException {
        RequestEthCall jsonRpcRequest = new RequestEthCall(jsonRpcService, request) {};
        Types.ResponseEthCall response = jsonRpcRequest.send().getResponse();
        log(request, response);
        return response;
    }

    @Override public Types.ResponseEthNewFilter ethNewFilter(Types.RequestEthNewFilter request) throws IOException {
        RequestEthNewFilter jsonRpcRequest = new RequestEthNewFilter(jsonRpcService, request) {};
        Types.ResponseEthNewFilter response = jsonRpcRequest.send().getResponse();
        log(request, response);
        return response;
    }

    @Override public Types.ResponseEthNewFilter ethNewBlockFilter() throws IOException {
        RequestEthNewBlockFilter jsonRpcRequest = new RequestEthNewBlockFilter(jsonRpcService) {};
        Types.ResponseEthNewFilter response = jsonRpcRequest.send().getResponse();
        log(Types.RequestEthNewFilter.newBuilder().build(), response);
        return response;
    }

    @Override public Types.ResponseEthUninstallFilter ethUninstallFilter(Types.RequestEthUninstallFilter request) throws IOException {
        RequestEthUninstallFilter jsonRpcRequest = new RequestEthUninstallFilter(jsonRpcService, request) {};
        Types.ResponseEthUninstallFilter response = jsonRpcRequest.send().getResponse();
        log(request, response);
        return response;
    }

    @Override public Types.ResponseEthGetFilterChanges ethGetFilterChanges(Types.RequestEthGetFilterChanges request) throws IOException {
        RequestEthGetFilterChanges jsonRpcRequest = new RequestEthGetFilterChanges(jsonRpcService, request) {};
        Types.ResponseEthGetFilterChanges response = jsonRpcRequest.send().getResponse();
        log(request, response);
        return response;
    }

    @Override public Types.ResponseEthGetTransactionReceipt ethGetTransactionReceipt(Types.RequestEthGetTransactionReceipt request) throws IOException {
        RequestEthGetTransactionReceipt jsonRpcRequest = new RequestEthGetTransactionReceipt(jsonRpcService, request) {};
        Types.ResponseEthGetTransactionReceipt response = jsonRpcRequest.send().getResponse();
        log(request, response);
        return response;
    }

    @Override public Observable<Types.FilterLogType> ethLogObservable(Types.RequestEthNewFilter request) {
        RequestEthNewFilter jsonRpcRequest = new RequestEthNewFilter(jsonRpcService, request) {};
        Observable<Types.FilterLogType> observable = jsonRpcRx.ethLogObservable(jsonRpcRequest, blockTime);
        log(request, Types.FilterLogType.getDescriptor(), observable);
        return observable;
    }
}