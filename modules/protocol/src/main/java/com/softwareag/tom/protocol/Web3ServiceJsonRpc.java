/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol;

import com.google.protobuf.Message;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Request;
import com.softwareag.tom.protocol.jsonrpc.Service;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthCall;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetBalance;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetFilterChanges;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetStorageAt;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter;
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthSendTransaction;
import com.softwareag.tom.protocol.jsonrpc.request.RequestNetListening;
import com.softwareag.tom.protocol.jsonrpc.request.RequestWeb3ClientVersion;

/**
 * Web3 over JSON-RPC service implementation.
 */
class Web3ServiceJsonRpc implements Web3Service {

    private final Service jsonRpcService;

    Web3ServiceJsonRpc(Service jsonRpcService) {
        this.jsonRpcService = jsonRpcService;
    }

    @Override public Message web3ClientVersion(Types.RequestWeb3ClientVersion req) {
        Request jsonRpcRequest = new RequestWeb3ClientVersion(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Message netListening(Types.RequestNetListening req) {
        Request jsonRpcRequest = new RequestNetListening(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Message ethGetBalance(Types.RequestEthGetBalance req) {
        Request jsonRpcRequest = new RequestEthGetBalance(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Message ethGetStorageAt(Types.RequestEthGetStorageAt req) {
        Request jsonRpcRequest = new RequestEthGetStorageAt(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Message ethSendTransaction(Types.RequestEthSendTransaction req) {
        Request jsonRpcRequest = new RequestEthSendTransaction(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Message ethCall(Types.RequestEthCall req) {
        Request jsonRpcRequest = new RequestEthCall(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Message ethNewFilter(Types.RequestEthNewFilter req) {
        Request jsonRpcRequest = new RequestEthNewFilter(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }

    @Override public Message ethGetFilterChanges(Types.RequestEthGetFilterChanges req) {
        Request jsonRpcRequest = new RequestEthGetFilterChanges(jsonRpcService, req) {};
        return jsonRpcRequest.send().getResponse();
    }
}