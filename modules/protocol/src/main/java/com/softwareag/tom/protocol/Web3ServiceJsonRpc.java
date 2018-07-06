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
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewBlockFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthSendTransaction;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthUninstallFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestNetListening;
import com.softwareag.tom.protocol.jsonrpc.request.RequestWeb3ClientVersion;
import com.softwareag.tom.protocol.tx.TransactionManager;
import rx.Observable;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Web3 over JSON-RPC service implementation.
 */
class Web3ServiceJsonRpc implements Web3Service {
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
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseNetListening netListening() throws IOException {
        RequestNetListening jsonRpcRequest = new RequestNetListening(jsonRpcService) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthGetBalance ethGetBalance(Types.RequestEthGetBalance req) throws IOException {
        RequestEthGetBalance jsonRpcRequest = new RequestEthGetBalance(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthGetStorageAt ethGetStorageAt(Types.RequestEthGetStorageAt req) throws IOException {
        RequestEthGetStorageAt jsonRpcRequest = new RequestEthGetStorageAt(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthSendTransaction ethSendTransaction(Types.RequestEthSendTransaction req) throws IOException {
        RequestEthSendTransaction jsonRpcRequest = new RequestEthSendTransaction(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthCall ethCall(Types.RequestEthCall req) throws IOException {
        RequestEthCall jsonRpcRequest = new RequestEthCall(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthNewFilter ethNewFilter(Types.RequestEthNewFilter req) throws IOException {
        RequestEthNewFilter jsonRpcRequest = new RequestEthNewFilter(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthNewFilter ethNewBlockFilter() throws IOException {
        RequestEthNewBlockFilter jsonRpcRequest = new RequestEthNewBlockFilter(jsonRpcService) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthUninstallFilter ethUninstallFilter(Types.RequestEthUninstallFilter req) throws IOException {
        RequestEthUninstallFilter jsonRpcRequest = new RequestEthUninstallFilter(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthGetFilterChanges ethGetFilterChanges(Types.RequestEthGetFilterChanges req) throws IOException {
        RequestEthGetFilterChanges jsonRpcRequest = new RequestEthGetFilterChanges(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthGetTransactionReceipt ethGetTransactionReceipt(Types.RequestEthGetTransactionReceipt req) {
        return Types.ResponseEthGetTransactionReceipt.newBuilder().setTxReceipt(TransactionManager.instance.getTransactionReceipt(req.getHash())).build();
    }

    @Override public Observable<Types.FilterLogType> ethLogObservable(Types.RequestEthNewFilter ethFilter) {
        RequestEthNewFilter jsonRpcRequest = new RequestEthNewFilter(jsonRpcService, ethFilter) {};
        return jsonRpcRx.ethLogObservable(jsonRpcRequest, blockTime);
    }
}