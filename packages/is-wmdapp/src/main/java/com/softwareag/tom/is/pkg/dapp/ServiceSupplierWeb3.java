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
import com.softwareag.tom.protocol.Web3Service;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.ServiceHttp;
import com.softwareag.tom.protocol.util.HexValue;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

import java.io.IOException;
import java.util.Objects;

public class ServiceSupplierWeb3 implements ServiceSupplier<Observer<Types.FilterLogType>,Subscription> {
    private Web3Service web3Service;

    ServiceSupplierWeb3(Node node) {
        web3Service = Web3Service.build(new ServiceHttp("http://" + node.getHost().getIp() + ':' + node.getHost().getWeb3().getPort()));
    }

    public ServiceSupplierWeb3(Web3Service web3Service) {
        this.web3Service = web3Service;
    }

    @Override public String call(Contract contract, String data) throws IOException {
        Types.RequestEthCall request = Types.RequestEthCall.newBuilder().setTx(
            Types.TxType.newBuilder().setTo(HexValue.toByteString(contract.getContractAddress())).setGas(HexValue.toByteString(contract.getGasLimit())).setGasPrice(HexValue.toByteString(contract.getGasPrice())).setData(HexValue.toByteString(data)).build()
        ).build();
        Types.ResponseEthCall response = web3Service.ethCall(request);
        return HexValue.toString(response.getReturn());
    }

    @Override public void sendTransaction(Contract contract, String data) throws IOException {
        String contractAddress = contract.getContractAddress();
        // eth_sendTransaction
        Types.TxType.Builder txBuilder = Types.TxType.newBuilder();
        if (contractAddress != null) {
            txBuilder.setTo(HexValue.toByteString(contractAddress));
        }
        txBuilder.setData(HexValue.toByteString(data)).setGas(HexValue.toByteString(contract.getGasLimit())).setGasPrice(HexValue.toByteString(contract.getGasPrice()));
        Types.RequestEthSendTransaction requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(txBuilder.build()).build();
        Types.ResponseEthSendTransaction responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction);
        // eth_getTransactionReceipt
        Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build();
        Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt);
        contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().getContractAddress());
        if (contract.getContractAddress() == null && contractAddress != null) {
            contract.setContractAddress(contractAddress);
        } else if (!Objects.equals(contract.getContractAddress(), contractAddress)) {
            throw new IllegalStateException("Returned contract address is different from known contract address!");
        }
    }

    @Override public Contract validateContract(Contract contract) throws IOException {
        if (contract.getContractAddress() == null) {
            throw new IllegalStateException("Contract address is null; deploy the contract first before using!");
        } else if (!contract.isValid()) {
            //TODO :: Replace with eth_getCode when available
            Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(contract.getContractAddress())).build();
            Types.ResponseEthGetBalance response = web3Service.ethGetBalance(request);
            return response.getBalance().equals(HexValue.toByteString(0)) ? contract.setValid(true) : contract;
        } else {
            return contract;
        }
    }

    @Override public Subscription subscribe(Contract contract, Observer<Types.FilterLogType> observer) {
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
            Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contract.getContractAddress())).build()
        ).build();
        Observable<Types.FilterLogType> ethLogObservable = web3Service.ethLogObservable(requestEthNewFilter); //TODO :: Make available with all service implementations
        return ethLogObservable.subscribe(observer);
    }
}