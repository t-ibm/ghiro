/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthCall;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetBalance;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetFilterChanges;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetStorageAt;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthSendTransaction;
import com.softwareag.tom.protocol.jsonrpc.request.RequestNetListening;
import com.softwareag.tom.protocol.jsonrpc.request.RequestWeb3ClientVersion;
import com.softwareag.tom.protocol.tx.TransactionManager;

/**
 * Web3 over JSON-RPC service implementation.
 */
class Web3ServiceJsonRpc implements Web3Service {

    private final Service jsonRpcService;

    Web3ServiceJsonRpc(Service jsonRpcService) {
        this.jsonRpcService = jsonRpcService;
    }

    @Override public Types.ResponseWeb3ClientVersion web3ClientVersion(Types.RequestWeb3ClientVersion req) {
        RequestWeb3ClientVersion jsonRpcRequest = new RequestWeb3ClientVersion(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseNetListening netListening(Types.RequestNetListening req) {
        RequestNetListening jsonRpcRequest = new RequestNetListening(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthGetBalance ethGetBalance(Types.RequestEthGetBalance req) {
        RequestEthGetBalance jsonRpcRequest = new RequestEthGetBalance(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthGetStorageAt ethGetStorageAt(Types.RequestEthGetStorageAt req) {
        RequestEthGetStorageAt jsonRpcRequest = new RequestEthGetStorageAt(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthSendTransaction ethSendTransaction(Types.RequestEthSendTransaction req) {
        RequestEthSendTransaction jsonRpcRequest = new RequestEthSendTransaction(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthCall ethCall(Types.RequestEthCall req) {
        RequestEthCall jsonRpcRequest = new RequestEthCall(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthNewFilter ethNewFilter(Types.RequestEthNewFilter req) {
        RequestEthNewFilter jsonRpcRequest = new RequestEthNewFilter(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthGetFilterChanges ethGetFilterChanges(Types.RequestEthGetFilterChanges req) {
        RequestEthGetFilterChanges jsonRpcRequest = new RequestEthGetFilterChanges(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Types.ResponseEthGetTransactionReceipt ethGetTransactionReceipt(Types.RequestEthGetTransactionReceipt req) {
        return Types.ResponseEthGetTransactionReceipt.newBuilder().setTxReceipt(TransactionManager.instance.getTransactionReceipt(req.getHash())).build();
    }
}