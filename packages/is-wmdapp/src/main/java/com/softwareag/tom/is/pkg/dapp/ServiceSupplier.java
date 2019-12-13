/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.contract.abi.ParameterType;
import com.softwareag.tom.protocol.util.HexValue;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @param <N> The contract's unique constructor, function, or event representation
 * @param <E> The event type
 * @param <O> The observer type
 * @param <S> The subscription type
 */
public interface ServiceSupplier<N,E,O,S> extends ContractSupplier<N> {

    /**
     * @param name The contract's function ns name
     * @param pipeline The input pipeline
     */
    default void call(N name, IData pipeline) throws IOException {
        String uri = getContractUri(name);
        String functionName = getFunctionUri(name);
        decodeFunctionOutput(ContractSupplier.getFunction(getContract(uri), functionName), pipeline, call(name, encodeInput(ContractSupplier.getFunction(getContract(uri), functionName), pipeline)));
    }
    /**
     * @param name The contract's function ns name
     * @param data The request data
     * @return the response's return value
     */
    default String call(N name, String data) throws IOException {
        String uri = getContractUri(name);
        Contract contract = validateContract(uri);
        String response = call(contract,data);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, getFunctionUri(name), contract.getContractAddress()});
        return  response;
    }
    /**
     * @param contract The contract
     * @param data The request data
     * @return the response's return value
     */
    String call(Contract contract, String data) throws IOException;

    /**
     * @param name The contract's function ns name
     * @param pipeline The input pipeline
     */
    default void sendTransaction(N name, IData pipeline) throws IOException {
        String uri = getContractUri(name);
        String functionName = getFunctionUri(name);
        sendTransaction(name, encodeInput(ContractSupplier.getFunction(getContract(uri), functionName), pipeline));
    }
    /**
     * @param name The contract's function ns name
     * @param data The request data
     */
    default void sendTransaction(N name, String data) throws IOException {
        String uri = getContractUri(name);
        Contract contract = validateContract(uri);
        sendTransaction(contract, data);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, getFunctionUri(name), contract.getContractAddress()});
    }
    /**
     * @param contract The contract
     * @param data The request data
     */
    void sendTransaction(Contract contract, String data) throws IOException;

    /**
     * @param uri The contract's location
     * @return the contract
     * @throws IOException if the contract cannot be accessed
     */
    default Contract validateContract(String uri) throws IOException {
        Contract contract = getContract(uri);
        return validateContract(contract);
    }
    /**
     * @param contract The contract
     * @return the contract
     * @throws IOException if the contract cannot be accessed
     */
    Contract validateContract(Contract contract) throws IOException;

    /**
     * @param name The contract's constructor, function, or event name
     * @return the contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    default String deployContract(N name) throws IOException {
        return deployContract(getContractUri(name));
    }
    /**
     * @param uri The contract's location
     * @return the contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    default String deployContract(String uri) throws IOException {
        loadContracts();
        Contract contract = getContract(uri);
        if (contract.getContractAddress() != null) {
            throw new IllegalStateException("Contract address not null; it seems the contract was already deployed!");
        } else {
            sendTransaction(contract, contract.getBinary());
        }
        return contract.getContractAddress();
    }

    /**
     * @param name The contract's function ns name
     * @param observer The event observer
     * @return a subscription for the given observer, if any
     */
    default S subscribe(N name, O observer) throws IOException {
        String uri = getContractUri(name);
        Contract contract = validateContract(uri);
        S subscription = subscribe(contract, observer);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_OBSERVABLE_LOG, new Object[]{uri, contract.getContractAddress()});
        return  subscription;
    }
    /**
     * @param contract The contract
     * @param observer The event observer
     * @return a subscription for the given observer, if any
     */
    S subscribe(Contract contract, O observer);

    /**
     * @param name The contract's event ns name
     * @param logEvent The received log event
     * @return the log event as a {@link Message}
     */
    default Message<E> decodeLogEvent(N name, E logEvent) {
        String uri = getContractUri(name);
        Contract contract = getContract(uri);
        String eventName = getEventUri(name);
        Message<E> message = decodeLogEvent(contract, eventName, logEvent);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_EVENT_LOG, new Object[]{uri, eventName, contract.getContractAddress()});
        return message;
    }
    /**
     * @param contract The contract
     * @param eventName The contract's event name
     * @param logEvent The received log event
     * @return the log event as a {@link Message}
     */
    Message<E> decodeLogEvent(Contract contract, String eventName, E logEvent);

    /**
     * @param name The contract's event ns name
     * @param logEvent The received log event
     * @return {@code true} if the received log event matches the contract's event name, {@code false} otherwise
     */
    default boolean isMatchingEvent(N name, E logEvent) {
        String uri = getContractUri(name);
        Contract contract = getContract(uri);
        String eventName = getEventUri(name);
        return isMatchingEvent(contract, eventName, logEvent);
    }
    /**
     * @param contract The contract
     * @param eventName The contract's event name
     * @param logEvent The received log event
     * @return {@code true} if the received log event matches the contract's event name, {@code false} otherwise
     */
    boolean isMatchingEvent(Contract contract, String eventName, E logEvent);

    static <T> String encodeInput(ContractInterface.Specification<T> function, IData pipeline) {
        IDataCursor pc = pipeline.getCursor();
        List<T> values = new ArrayList<>();
        List<? extends ContractInterface.Parameter<T>> inputParameters = function.getInputParameters();
        for (ContractInterface.Parameter<T> parameter : inputParameters) {
            ParameterType<T> parameterType = parameter.getType();
            T value = parameterType.asType(IDataUtil.get(pc, parameter.getName()));
            values.add(value);
        }
        return function.encode(values);
    }
    static <T> void decodeFunctionOutput(ContractInterface.Specification<T> specification, IData pipeline, String data) {
        List<? extends ContractInterface.Parameter<T>> parameters = specification.getOutputParameters();
        List<T> values = specification.decode(parameters, data);
        decodeParameters(pipeline, parameters, values);
    }
    static <T> void decodeEventInput(ContractInterface.Specification<T> specification, IData pipeline, String data, List<String> topics) {
        List<? extends ContractInterface.Parameter<T>> nonIndexedParameters = specification.getInputParameters(false);
        List<T> nonIndexedValues = specification.decode(nonIndexedParameters, data);
        decodeParameters(pipeline, nonIndexedParameters, nonIndexedValues);
        List<? extends ContractInterface.Parameter<T>> indexedParameters = specification.getInputParameters(true);
        List<T> indexedValues = new ArrayList<>();
        for (int i = 0; i < indexedParameters.size(); i++) {
            String input = HexValue.stripPrefix(topics.get(i + 1));
            indexedValues.add(indexedParameters.get(i).decode(input));
        }
        decodeParameters(pipeline, indexedParameters, indexedValues);
    }
    static <T> void decodeParameters(IData pipeline, List<? extends ContractInterface.Parameter<T>> parameters, List<T> values) {
        assert values.size() == parameters.size();
        Iterator<? extends ContractInterface.Parameter<T>> parametersIterator = parameters.iterator();
        Iterator<T> valuesIterator = values.iterator();
        IDataCursor pc = pipeline.getCursor();
        while (parametersIterator.hasNext() && valuesIterator.hasNext()) {
            IDataUtil.put(pc, parametersIterator.next().getName(), valuesIterator.next());
        }
    }
}