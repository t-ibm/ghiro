/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.protocol.BurrowService;
import com.softwareag.tom.protocol.api.BurrowEvents;
import com.softwareag.tom.protocol.api.BurrowQuery;
import com.softwareag.tom.protocol.api.BurrowTransact;
import com.softwareag.tom.protocol.grpc.ServiceEvents;
import com.softwareag.tom.protocol.grpc.ServiceQuery;
import com.softwareag.tom.protocol.grpc.ServiceTransact;
import com.softwareag.tom.protocol.grpc.stream.StreamObserverSubscriber;
import com.softwareag.tom.protocol.grpc.stream.Subscription;
import com.softwareag.tom.protocol.util.HexValue;
import com.wm.app.b2b.server.dispatcher.Dispatcher;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.msg.Header;
import com.wm.util.Values;
import io.grpc.stub.StreamObserver;
import org.hyperledger.burrow.Acm;
import org.hyperledger.burrow.execution.Exec;
import org.hyperledger.burrow.rpc.RpcEvents;
import org.hyperledger.burrow.rpc.RpcQuery;
import org.hyperledger.burrow.txs.Payload;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServiceSupplierBurrow<N> extends ServiceSupplierBase<N,RpcEvents.EventsResponse,StreamObserver<RpcEvents.EventsResponse>,Subscription> {

    private BurrowTransact burrowTransact;
    private BurrowQuery burrowQuery;
    private BurrowEvents burrowEvents;

    ServiceSupplierBurrow(UtilBase<N> util) {
        this(
            util,
            BurrowService.query(new ServiceQuery(util.node.getHost().getIp(), util.node.getHost().getGRpc().getPort())),
            BurrowService.transact(new ServiceTransact(util.node.getHost().getIp(), util.node.getHost().getGRpc().getPort())),
            BurrowService.events(new ServiceEvents(util.node.getHost().getIp(), util.node.getHost().getGRpc().getPort()))
        );
    }

    public ServiceSupplierBurrow(UtilBase<N> util, BurrowQuery burrowQuery, BurrowTransact burrowTransact, BurrowEvents burrowEvents) {
        super(util);
        this.burrowQuery = burrowQuery;
        this.burrowTransact = burrowTransact;
        this.burrowEvents = burrowEvents;
    }

    @Override public void runContract(N name, IData pipeline, boolean transactional) throws IOException {
        call(name, pipeline);
    }

    @Override public void sendPayment(N name, IData pipeline) throws IOException {
        sendTransaction(name, pipeline);
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

    @Override public Subscription subscribe(Contract contract, StreamObserver<RpcEvents.EventsResponse> observer) {
        String contractAddressUpperCase = HexValue.stripPrefix(contract.getContractAddress()).toUpperCase(); //TODO :: Fix contract address
        String query = String.format("EventType = 'LogEvent' AND Address = '%s'", contractAddressUpperCase);
        RpcEvents.BlocksRequest request = RpcEvents.BlocksRequest.newBuilder().setBlockRange(
            RpcEvents.BlockRange.newBuilder()
                .setStart(RpcEvents.Bound.newBuilder().setType(RpcEvents.Bound.BoundType.LATEST))
                .setEnd(RpcEvents.Bound.newBuilder().setType(RpcEvents.Bound.BoundType.STREAM))
        ).setQuery(query).build();
        burrowEvents.getEvents(request, observer);
        return new StreamObserverSubscriber(burrowEvents.getService());
    }

    @Override public Message<RpcEvents.EventsResponse> decodeLogEvent(Contract contract, String eventName, RpcEvents.EventsResponse logEvent) {
        IData pipeline = IDataFactory.create();
        IData envelope = IDataFactory.create();
        String uuid = "" + logEvent.getEvents(0).getHeader().getHeight();
        IDataUtil.put(envelope.getCursor(),"uuid", uuid);
        IDataUtil.put(pipeline.getCursor(), Dispatcher.ENVELOPE_KEY, envelope);
        List<String> topics = logEvent.getEvents(0).getLog().getTopicsList().stream().map(HexValue::toString).collect(Collectors.toList());
        decodeEventInput(ContractSupplier.getEvent(contract,eventName), pipeline, HexValue.toString(logEvent.getEvents(0).getLog().getData().toByteArray()), topics);
        return new Message<RpcEvents.EventsResponse>() {
            {
                _event = logEvent;
                _msgID = uuid;
                _type = eventName;
                _data = pipeline;
            }

            @Override public Header getHeader(String name) { return null; }
            @Override public Header[] getHeaders() { return new Header[0]; }
            @Override public void setData(Object o) { _data = (IData)o; }
            @Override public Values getValues() { return Values.use(_data); }
        };
    }

    @Override public boolean isMatchingEvent(Contract contract, String eventName, RpcEvents.EventsResponse logEvent) {
        String actual = HexValue.stripPrefix(HexValue.toString(logEvent.getEvents(0).getLog().getTopics(0)));
        String expected = ContractSupplier.getEvent(contract, eventName).encode();
        return actual.equalsIgnoreCase(expected);
    }
}