/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.conf.Node;
import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.protocol.BurrowService;
import com.softwareag.tom.protocol.api.BurrowEvents;
import com.softwareag.tom.protocol.api.BurrowQuery;
import com.softwareag.tom.protocol.api.BurrowTransact;
import com.softwareag.tom.protocol.grpc.ServiceEvents;
import com.softwareag.tom.protocol.grpc.ServiceQuery;
import com.softwareag.tom.protocol.grpc.ServiceTransact;
import com.softwareag.tom.protocol.util.HexValue;
import io.grpc.stub.StreamObserver;
import org.hyperledger.burrow.Acm;
import org.hyperledger.burrow.execution.Exec;
import org.hyperledger.burrow.rpc.RpcEvents;
import org.hyperledger.burrow.rpc.RpcQuery;
import org.hyperledger.burrow.txs.Payload;

import java.util.Objects;

public class ServiceSupplierBurrow implements ServiceSupplier<StreamObserver<RpcEvents.EventsResponse>,Object> {
    private BurrowTransact burrowTransact;
    private BurrowQuery burrowQuery;
    private BurrowEvents burrowEvents;

    ServiceSupplierBurrow(Node node) {
        burrowQuery = BurrowService.query(new ServiceQuery(node.getHost().getIp(), node.getHost().getGRpc().getPort()));
        burrowTransact = BurrowService.transact(new ServiceTransact(node.getHost().getIp(), node.getHost().getGRpc().getPort()));
        burrowEvents = BurrowService.events(new ServiceEvents(node.getHost().getIp(), node.getHost().getGRpc().getPort()));
    }

    public ServiceSupplierBurrow(BurrowQuery burrowQuery, BurrowTransact burrowTransact, BurrowEvents burrowEvents) {
        this.burrowQuery = burrowQuery;
        this.burrowTransact = burrowTransact;
        this.burrowEvents = burrowEvents;
    }

    @Override public String call(Contract contract, String data) {
        String caller = "0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19"; //TODO :: Get this from burrow.toml or similar instead
        Payload.TxInput txInput = Payload.TxInput.newBuilder().setAddress(HexValue.copyFrom(caller)).setAmount(20).build();
        Payload.CallTx requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setAddress(HexValue.copyFrom(contract.getContractAddress())).setGasLimit(contract.getGasLimit().longValue()).setGasPrice(contract.getGasPrice().longValue()).setFee(20).setData(HexValue.copyFrom(data)).build();
        Exec.TxExecution responseTxExecution = burrowTransact.callTx(requestCallTx);
        return HexValue.toString(responseTxExecution.getResult().getReturn());
    }

    @Override public void sendTransaction(Contract contract, String data) {
        String caller = "0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19"; //TODO :: Get this from burrow.toml or similar instead
        String contractAddress = contract.getContractAddress();

        // eth_sendTransaction
        Payload.TxInput txInput = Payload.TxInput.newBuilder().setAddress(HexValue.copyFrom(caller)).setAmount(20).build();
        Payload.TxOutput.Builder txOutputBuilder = Payload.TxOutput.newBuilder();
        if (contractAddress != null) {
            txOutputBuilder.setAddress(HexValue.copyFrom(contractAddress));
        }
        Payload.CallTx requestCallTx = Payload.CallTx.newBuilder().setInput(txInput).setGasLimit(contract.getGasLimit().longValue()).setGasPrice(contract.getGasPrice().longValue()).setFee(20).setData(HexValue.copyFrom(data)).build();
        Exec.TxExecution responseTxExecution = burrowTransact.callTx(requestCallTx);
        // eth_getTransactionReceipt
        contractAddress = HexValue.toString(responseTxExecution.getReceipt().getContractAddress().toByteArray());
        if (contract.getContractAddress() == null) {
            contract.setContractAddress(contractAddress);
        } else if (!Objects.equals(contract.getContractAddress(), contractAddress)) {
            throw new IllegalStateException("Returned contract address is different from known contract address!");
        }
    }

    @Override public Contract validateContract(Contract contract) {
        if (contract.getContractAddress() == null) {
            throw new IllegalStateException("Contract address is null; deploy the contract first before using!");
        } else if (!contract.isValid()) {
            RpcQuery.GetAccountParam requestGetAccountParam = RpcQuery.GetAccountParam.newBuilder().setAddress(HexValue.copyFrom(contract.getContractAddress())).build();
            Acm.Account responseAccount = burrowQuery.getAccount(requestGetAccountParam);
            return responseAccount.getEVMCode() != null ? contract.setValid(true) : contract;
        } else {
            return contract;
        }
    }

    @Override public Object subscribe(Contract contract, StreamObserver<RpcEvents.EventsResponse> observer) {
        String contractAddressUpperCase = HexValue.stripPrefix(contract.getContractAddress()).toUpperCase(); //TODO :: Fix contract address
        String query = String.format("EventType = 'LogEvent' AND Address = '%s'", contractAddressUpperCase);
        RpcEvents.BlocksRequest request = RpcEvents.BlocksRequest.newBuilder().setBlockRange(
            RpcEvents.BlockRange.newBuilder()
                .setStart(RpcEvents.Bound.newBuilder().setType(RpcEvents.Bound.BoundType.LATEST))
                .setEnd(RpcEvents.Bound.newBuilder().setType(RpcEvents.Bound.BoundType.STREAM))
        ).setQuery(query).build();
        burrowEvents.getEvents(request, observer);
        return null;
    }
}