/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.conf.Node;
import com.softwareag.tom.contract.ConfigLocationFileSystem;
import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.contract.ContractRegistry;
import com.softwareag.tom.contract.SolidityLocationFileSystem;
import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.contract.abi.ParameterType;
import com.softwareag.tom.is.pkg.dapp.trigger.Condition;
import com.softwareag.tom.protocol.Web3Service;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.ServiceHttp;
import com.softwareag.tom.protocol.util.HexValue;
import com.wm.app.b2b.broker.conv.Transformer;
import com.wm.app.b2b.broker.conv.TypeCoderException;
import com.wm.app.b2b.broker.sync.SyncException;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.NodeFactory;
import com.wm.app.b2b.server.NodeMaster;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.app.b2b.server.dispatcher.DispatchFacade;
import com.wm.app.b2b.server.dispatcher.Dispatcher;
import com.wm.app.b2b.server.dispatcher.exceptions.MessagingSubsystemException;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.app.b2b.server.dispatcher.wmmessaging.ConnectionAlias;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.app.b2b.server.dispatcher.wmmessaging.RuntimeConfiguration;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.app.b2b.ws.codegen.FlowGenUtil;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.flow.FlowInvoke;
import com.wm.lang.flow.FlowMap;
import com.wm.lang.flow.FlowMapSet;
import com.wm.lang.flow.FlowRoot;
import com.wm.lang.ns.EventDescription;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSServiceType;
import com.wm.lang.ns.NSSignature;
import com.wm.lang.ns.NSTrigger;
import com.wm.msg.Header;
import com.wm.util.JavaWrapperType;
import com.wm.util.Values;
import rx.Observable;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.softwareag.tom.is.pkg.dapp.trigger.DAppListener.IS_DAPP_CONNECTION;

public class Util {
    static final String SUFFIX_REQ = "Req";
    static final String SUFFIX_DOC = "Doc";
    static final String SUFFIX_TRG = "Trg";
    static final String SUFFIX_REP = "Rep";

    public static Util instance = new Util();
    public static RuntimeConfiguration rt;

    private Package pkgWmDApp = PackageManager.getPackage("WmDApp");
    private Package pkgWmDAppContract = PackageManager.getPackage("WmDAppContract");
    private ContractRegistry contractRegistry;
    private Map<String,Contract> contracts;
    private Map<String,FlowSvcImpl> functions;
    private Map<String,Event> events;

    public Web3Service web3Service;

    /**
     * The default constructor.
     * @throws ExceptionInInitializerError if the node configuration is missing
     */
    private Util() throws ExceptionInInitializerError {
        functions = new HashMap<>();
        events = new HashMap<>();
        System.setProperty(Node.SYSTEM_PROPERTY_TOMCONFNODE, pkgWmDApp == null ? "default" : String.valueOf(pkgWmDApp.getManifest().getProperty("node")));
        try {
            URI contractRegistryLocation = Node.instance().getContract().getRegistry().getLocationAsUri();
            URI configLocation = Node.instance().getConfig().getLocationAsUri();
            contractRegistry = ContractRegistry.build(new SolidityLocationFileSystem(contractRegistryLocation), new ConfigLocationFileSystem(configLocation));
            web3Service = Web3Service.build(new ServiceHttp("http://" + Node.instance().getHost().getIp() + ':' + Node.instance().getHost().getPort() + "/rpc"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * @param nsName The contract's function ns name
     * @param pipeline The input pipeline
     */
    public void call(NSName nsName, IData pipeline) throws IOException {
        String uri = getContractUri(nsName);
        Contract contract = validate(contracts.get(uri));
        ContractInterface.Specification<?> function = getFunction(nsName);
        Types.RequestEthCall request = Types.RequestEthCall.newBuilder().setTx(
            Types.TxType.newBuilder().setTo(HexValue.toByteString(contract.getContractAddress())).setData(HexValue.toByteString(encodeInput(function, pipeline))).build()
        ).build();
        Types.ResponseEthCall response = web3Service.ethCall(request);
        decodeFunctionOutput(function, pipeline, HexValue.toString(response.getReturn()));
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, getFunctionName(nsName), contract.getContractAddress()});
    }

    /**
     * @param nsName The contract's function ns name
     * @param pipeline The input pipeline
     */
    public void sendTransaction(NSName nsName, IData pipeline) throws IOException {
        String uri = getContractUri(nsName);
        Contract contract = validate(contracts.get(uri));
        ContractInterface.Specification<?> function = getFunction(nsName);
        sendTransaction(contract, encodeInput(function, pipeline));
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, getFunctionName(nsName), contract.getContractAddress()});
    }

    /**
     * @param nsName The contract's filter ns name
     * @return the corresponding log observable
     */
    public Observable<Types.FilterLogType> getLogObservable(NSName nsName) throws IOException {
        String uri = getContractUri(nsName);
        Contract contract = validate(contracts.get(uri));
        String eventSignatureHash = getEvent(nsName).encode();
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
            Types.FilterOptionType.newBuilder()
                .setAddress(HexValue.toByteString(contract.getContractAddress()))
                .addTopic(HexValue.toByteString(eventSignatureHash))
                .build()
        ).build();
        Observable<Types.FilterLogType> ethLogObservable = web3Service.ethLogObservable(requestEthNewFilter);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_OBSERVABLE_LOG, new Object[]{uri, contract.getContractAddress()});
        return ethLogObservable;
    }

    public boolean isMatchingEvent(NSName nsName, Types.FilterLogType logEvent) {
        String actual = HexValue.stripPrefix(HexValue.toString(logEvent.getTopic(0)));
        String expected = getEvent(nsName).encode();
        return actual.equalsIgnoreCase(expected);
    }

    /**
     * @param nsName The contract's event ns name
     * @return the data pipeline wrapped as a {@link Message}
     */
    public Message<Types.FilterLogType> decodeLogEvent(NSName nsName, Types.FilterLogType logEvent) {
        String uri = getContractUri(nsName);
        Contract contract = contracts.get(uri);
        ContractInterface.Specification<?> event = getEvent(nsName);
        IData pipeline = IDataFactory.create();
        IData envelope = IDataFactory.create();
        String uuid = HexValue.toString(logEvent.getTransactionIndex());
        IDataUtil.put(envelope.getCursor(),"uuid", uuid);
        IDataUtil.put(pipeline.getCursor(), Dispatcher.ENVELOPE_KEY, envelope);
        List<String> topics = logEvent.getTopicList().stream().map(HexValue::toString).collect(Collectors.toList());
        decodeEventInput(event, pipeline, HexValue.toString(logEvent.getData()), topics);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_EVENT_LOG, new Object[]{uri, getEventName(nsName), contract.getContractAddress()});
        return new Message<Types.FilterLogType>() {
            {
                _event = logEvent;
                _msgID = uuid;
                _type = nsName.getFullName();
                _data = pipeline;

            }

            @Override public Header getHeader(String name) { return null; }
            @Override public Header[] getHeaders() { return new Header[0]; }
            @Override public void setData(Object o) { _data = (IData)o; }
            @Override public Values getValues() { return Values.use(_data); }
        };
    }

    /**
     * @param name The contract's local location, ns name, or ns node
     * @return {@code true} if the contract was already deployed, {@code false} otherwise
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public boolean isContractDeployed(Object name) throws IOException {
        String uri;
        if (name instanceof NSName) {
            uri = getContractUri((NSName)name);
        } else if (name instanceof NSNode) {
            uri = getContractUri(((NSNode)name).getNSName());
        } else {
            uri = (String)name;
        }
        contracts = contractRegistry.load();
        Contract contract = contracts.get(uri);
        return contract.getContractAddress() != null;
    }

    /**
     * @param uri The contract's local location
     * @return the contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public String deployContract(String uri) throws IOException {
        contracts = contractRegistry.load();
        Contract contract = contracts.get(uri);
        if (contract.getContractAddress() != null) {
            throw new IllegalStateException("Contract address not null; it seems the contract was already deployed!");
        } else {
            sendTransaction(contract, contract.getBinary());
        }
        return contract.getContractAddress();
    }

    /**
     * @param nsName The contract's function/filter ns name
     * @return the contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public String deployContract(NSName nsName) throws IOException {
        return deployContract(getContractUri(nsName));
    }

    /**
     * @return the contracts-address mapping as a {@link IData} list with fields {@code uri} and {@code contractAddress}
     */
    public IData[] getContractAddresses() throws IOException {
        contracts = contractRegistry.load();
        IData[] contractArray = new IData[contracts.size()];
        List<IData> contractList = contracts.entrySet().stream().map(entry -> IDataFactory.create(new Object[][]{
            {"uri", entry.getKey()},
            {"address", entry.getValue().getContractAddress()},
        })).collect(Collectors.toList());
        return contractList.toArray(contractArray);
    }

    /**
     * @param uri The contract's local location
     * @param contractAddress The contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public void storeContractAddress(String uri, String contractAddress) throws IOException {
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_DEPLOY, new Object[]{uri, contractAddress});
        contracts = contractRegistry.load();
        contracts.put(uri, contracts.get(uri).setContractAddress(contractAddress));
        contracts = contractRegistry.storeContractAddresses(contracts);
    }

    /**
     * @param nsName The contract's function/filter ns name
     * @param contractAddress The contract's address in the distributed ledger
     * @throws IOException if loading/storing of the contract-address mapping fails
     */
    public void storeContractAddress(NSName nsName, String contractAddress) throws IOException {
        storeContractAddress(getContractUri(nsName), contractAddress);
    }

    /**
     * @param deployedOnly If set to {@code true} returns only functions from deployed contracts, otherwise returns all defined
     * @return the contract functions as a {@link NSName}/{@link FlowSvcImpl} map
     */
    public Map<String,FlowSvcImpl> getFunctions(boolean deployedOnly) throws IOException {
        contracts = contractRegistry.load();
        for (Map.Entry<String,Contract> entry : contracts.entrySet()) {
            // Add the functions as defined in the ABI
            String interfaceName = getInterfaceName(entry.getKey());
            Contract contract = entry.getValue();
            if (deployedOnly && contract.getContractAddress() == null) {
                continue;
            }
            ContractInterface contractInterface = contract.getAbi();
            List<ContractInterface.Specification> functions = contractInterface.getFunctions();
            for (ContractInterface.Specification<?> function : functions) {
                String functionName = interfaceName + ':' + function.getName();
                NSName nsName = NSName.create(functionName + SUFFIX_REQ);
                NSSignature nsSignature = getFunctionSignature(functionName, function);
                FlowInvoke flowInvoke = new FlowInvoke(IDataFactory.create());
                FlowSvcImpl flowSvcImpl;
                if (function.isConstant()) {
                    flowInvoke.setService(NSName.create("wm.dapp.Contract:call"));
                    flowSvcImpl = createFlowSvcImpl(nsName, nsSignature, flowInvoke, NSServiceType.create(NSServiceType.SVC_FLOW, "dapp")); // TODO :: Maybe add global field to NSServiceType.SVCSUB_DAPP
                    flowSvcImpl.setStateless(true);
                } else {
                    flowInvoke.setService(NSName.create("wm.dapp.Contract:sendTransaction"));
                    flowSvcImpl = createFlowSvcImpl(nsName, nsSignature, flowInvoke, NSServiceType.create(NSServiceType.SVC_FLOW, "dapp")); // TODO :: Maybe add global field to NSServiceType.SVCSUB_DAPP
                    flowSvcImpl.setStateless(false);
                }
                this.functions.put(functionName, flowSvcImpl);
            }
        }
        return functions;
    }

    /**
     * @param deployedOnly If set to {@code true} returns only events from deployed contracts, otherwise returns all defined
     * @return the contract events as a {@link Trigger}/{@link NSRecord} map
     */
    public Map<String,Event> getEvents(boolean deployedOnly) throws Exception {
        contracts = contractRegistry.load();
        for (Map.Entry<String,Contract> entry : contracts.entrySet()) {
            // Add the events as defined in the ABI
            String interfaceName = getInterfaceName(entry.getKey());
            Contract contract = entry.getValue();
            if (deployedOnly && contract.getContractAddress() == null) {
                continue;
            }
            ContractInterface contractInterface = contract.getAbi();
            List<ContractInterface.Specification> events = contractInterface.getEvents();
            // Remember all record ns nodes for this contract
            Map<NSName,NSRecord> nsRecords = new HashMap<>();
            for (ContractInterface.Specification<?> event : events) {
                String eventName = interfaceName + ':' + event.getName();
                // Nested service
                FlowInvoke flowInvoke = FlowGenUtil.getFlowInvoke("pub.flow:debugLog");
                FlowMap fm = FlowGenUtil.getFlowMap();
                FlowMapSet fms = FlowGenUtil.getFlowMapSet("message", NSField.FIELD_STRING, NSField.DIM_SCALAR, "Processing event");
                fm.addNode(fms);
                fms = FlowGenUtil.getFlowMapSet("function", NSField.FIELD_STRING, NSField.DIM_SCALAR, eventName);
                fm.addNode(fms);
                fms = FlowGenUtil.getFlowMapSet("level", NSField.FIELD_STRING, NSField.DIM_SCALAR, "Info");
                fm.addNode(fms);
                flowInvoke.setInputMap(fm);
                // Response service
                NSName svcNsName = NSName.create(eventName + SUFFIX_REP);
                NSSignature nsSignature = getEventSignature(eventName, event);
                FlowSvcImpl flowSvcImpl = createFlowSvcImpl(svcNsName, nsSignature, flowInvoke, NSServiceType.create(NSServiceType.SVC_FLOW,NSServiceType.SVCSUB_UNKNOWN));
                // The record name
                NSName pdtNsName = NSName.create(eventName + SUFFIX_DOC);
                // Ensure a folder for the ns node exists
                mkdirs(pdtNsName);
                // Get the record ns node
                NSRecord pdt = getPublishableDocumentType(pdtNsName);
                // If the same ns node with a different signature already exists we simply add to the existing signature ...
                pdt = nsRecords.getOrDefault(pdtNsName, pdt);
                // ... but make the parameters optional
                boolean optional = nsRecords.containsKey(pdtNsName);
                // Set the record fields as defined by the event input parameters
                NSRecord inputRecord = getNsRecord(event.getInputParameters(), optional);
                pdt.mergeRecord(inputRecord);
                // Remember this record ns node
                nsRecords.put(pdtNsName, pdt);
                // The trigger name
                NSName triggerNsName = NSName.create(interfaceName, event.getName() + SUFFIX_TRG);
                Trigger trigger = getTrigger(triggerNsName, Collections.singletonList(Condition.create(pdtNsName, svcNsName)));
                // Add to the event condition map
                this.events.put(eventName, Event.create(trigger, pdt, flowSvcImpl));
            }
        }
        return events;
    }

    public NSRecord getPublishableDocumentType(NSName nsName) throws SyncException, TypeCoderException, MessagingSubsystemException {
        rt = rt != null ? rt : DispatchFacade.getRuntimeConfiguration();

        ConnectionAlias alias = rt.getConnectionAlias(IS_DAPP_CONNECTION);
        NSRecord nsRecord = new NSRecord(Namespace.current(), nsName.getFullName(), NSRecord.DIM_SCALAR);
        nsRecord.setNSName(nsName);
        nsRecord.setPackage(pkgWmDAppContract);
        IData result = Transformer.transformTo(Namespace.current(), nsRecord, 0, EventDescription.VOLATILE, false, alias, IS_DAPP_CONNECTION, false, false);
        assert result.equals(IDataFactory.create(new Object[][]{{"isSuccessful","true"}}));
//        EventDescription eventDescription = EventDescription.create(IS_DAPP_CONNECTION, Name.create(nsName.getFullName()), 0, EventDescription.VOLATILE);
//        NSRecordUtil.transform(nsRecord, eventDescription);
        return nsRecord;
    }

    public FlowSvcImpl getResponseService(NSName nsName) {
        return new FlowSvcImpl(pkgWmDAppContract, nsName, null);
    }

    public Trigger getTrigger(NSName nsName, Collection<Condition> triggerConditions) {
        IData[] conditions = triggerConditions.stream().map(Condition::asIData).toArray(IData[]::new);
        NodeFactory nf = NodeMaster.getFactory(NSTrigger.TYPE.getType());
        IData nodeDef = IDataFactory.create(new Object[][]{
            {NSNode.KEY_NSN_NSNAME, nsName.getFullName()},
            {NSNode.KEY_NSN_TYPE, NSTrigger.TYPE_KEY},
            {NSTrigger.KEY_TRIGGER, IDataFactory.create(new Object[][]{
                {"conditions", conditions},
            })},
        });
        Trigger trigger = (Trigger)nf.createFromNodeDef(pkgWmDAppContract, nsName, Values.use(nodeDef));
        trigger.setPackage(pkgWmDAppContract);
        return trigger;
    }

    private <T> String encodeInput(ContractInterface.Specification<T> function, IData pipeline) {
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

    private <T> void decodeEventInput(ContractInterface.Specification<T> specification, IData pipeline, String data, List<String> topics) {
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

    private <T> void decodeFunctionOutput(ContractInterface.Specification<T> specification, IData pipeline, String data) {
        List<? extends ContractInterface.Parameter<T>> parameters = specification.getOutputParameters();
        List<T> values = specification.decode(parameters, data);
        decodeParameters(pipeline, parameters, values);
    }

    private <T> void decodeParameters(IData pipeline, List<? extends ContractInterface.Parameter<T>> parameters, List<T> values) {
        assert values.size() == parameters.size();
        Iterator<? extends ContractInterface.Parameter<T>> parametersIterator = parameters.iterator();
        Iterator<T> valuesIterator = values.iterator();
        IDataCursor pc = pipeline.getCursor();
        while (parametersIterator.hasNext() && valuesIterator.hasNext()) {
            IDataUtil.put(pc, parametersIterator.next().getName(), valuesIterator.next());
        }
    }

    private Contract validate(Contract contract) throws IOException {
        if (contract.getContractAddress() == null) {
            throw new IllegalStateException("Contract address is null; deploy the contract first before using!");
        } else if (!contract.isValid()) {
            //TODO :: Replace with eth_getCode when available
            Types.RequestEthGetBalance request = Types.RequestEthGetBalance.newBuilder().setAddress(HexValue.toByteString(contract.getContractAddress())).build();
            Types.ResponseEthGetBalance response = web3Service.ethGetBalance(request);
            return response.getBalance() == HexValue.toByteString(0) ? contract.setValid(true) : contract;
        } else {
            return contract;
        }
    }

    private void sendTransaction(Contract contract, String data) throws IOException {
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

    private String getInterfaceName(String uri) {
        return uri.replace('/', '.');
    }

    private String getContractUri(NSName nsName) {
        return nsName.getInterfaceName().toString().replace('.', '/');
    }

    private String getFunctionName(NSName nsName) {
        String name = nsName.getNodeName().toString();
        return name.endsWith(SUFFIX_REQ) ? name.substring(0, name.length() - SUFFIX_REQ.length()) : name;
    }

    private String getEventName(NSName nsName) {
        String name = nsName.getNodeName().toString();
        return name.endsWith(SUFFIX_DOC) ? name.substring(0, name.length() - SUFFIX_DOC.length()) : name;
    }

    private ContractInterface.Specification<?> getFunction(NSName nsName) {
        String uri = getContractUri(nsName);
        String functionName = getFunctionName(nsName);
        Contract contract = contracts.get(uri);
        ContractInterface.Specification<?> function = contract.getAbi().getFunctions().stream().filter(o -> o.getName().equals(functionName)).findFirst().orElse(null);
        assert function != null;
        return function;
    }

    private ContractInterface.Specification<?> getEvent(NSName nsName) {
        String uri = getContractUri(nsName);
        String eventName = getEventName(nsName);
        Contract contract = contracts.get(uri);
        ContractInterface.Specification<?> event = contract.getAbi().getEvents().stream().filter(o -> o.getName().equals(eventName)).findFirst().orElse(null);
        assert event != null;
        return event;
    }

    private FlowSvcImpl createFlowSvcImpl(NSName nsName, NSSignature nsSignature, FlowInvoke flowInvoke, NSServiceType serviceType) {
        mkdirs(nsName);

        FlowSvcImpl flowSvcImpl = new FlowSvcImpl(pkgWmDAppContract, nsName, null);
        flowSvcImpl.setServiceSigtype(NSService.SIG_JAVA_3_5);
        flowSvcImpl.setFlowRoot(new FlowRoot(IDataFactory.create()));
        flowSvcImpl.setServiceType(serviceType);

        if (nsSignature != null) {
            flowSvcImpl.setSignature(nsSignature);
        } else {
            flowSvcImpl.setSignature(new NSSignature(new NSRecord(Namespace.current()), new NSRecord(Namespace.current())));
        }


        flowSvcImpl.getFlowRoot().addNode(flowInvoke);
        return flowSvcImpl;
    }

    private <T> NSSignature getFunctionSignature(String functionName, ContractInterface.Specification<T> function) {
        // If the same ns node with a different signature already exists we simply add to the existing signature ...
        NSSignature nsSignature = functions.containsKey(functionName) ? functions.get(functionName).getSignature() : NSSignature.create(Namespace.current(), IDataFactory.create());
        // ... but make the parameters optional
        boolean optional = functions.containsKey(functionName);

        // Input
        NSRecord inputRecord = getNsRecord(function.getInputParameters(), optional);
        nsSignature.setInput(inputRecord);

        // Output
        NSRecord outputRecord = getNsRecord(function.getOutputParameters(), optional);
        nsSignature.setOutput(outputRecord);

        return nsSignature;
    }

    private <T> NSSignature getEventSignature(String eventName, ContractInterface.Specification<T> event) {
        // If the same ns node with a different signature already exists we simply add to the existing signature ...
        NSSignature nsSignature = events.containsKey(eventName) ? events.get(eventName).getService().getSignature() : NSSignature.create(Namespace.current(), IDataFactory.create());
        // ... but make the parameters optional
        boolean optional = events.containsKey(eventName);

        // Input
        NSRecord inputRecord = getNsRecord(event.getInputParameters(), optional);
        nsSignature.setInput(inputRecord);

        return nsSignature;
    }

    private void mkdirs(NSName nsName) {
        if (pkgWmDAppContract != null && !pkgWmDAppContract.getStore().getNodePath(nsName).mkdirs()) {
            DAppLogger.logDebug(DAppMsgBundle.DAPP_SERVICES_MKDIRS, new Object[]{"" + nsName});
        }
    }

    private <T> NSRecord getNsRecord(List<? extends ContractInterface.Parameter<T>> parameters, boolean optional) {
        NSRecord nsRecord = new NSRecord(Namespace.current());
        for (ContractInterface.Parameter parameter : parameters) {
            String parameterName = parameter.getName();
            ParameterType parameterType = parameter.getType();
            if (parameterType.getType() == String.class) {
                nsRecord.addField(parameterName, NSField.FIELD_STRING, NSField.DIM_SCALAR);
            } else if (parameterType.getType() == List.class) {
                nsRecord.addField(parameterName, NSField.FIELD_OBJECT, NSField.DIM_ARRAY);
            } else {
                nsRecord.addField(parameterName, NSField.FIELD_OBJECT, NSField.DIM_SCALAR).setJavaWrapperType(getJavaWrapperType(parameterType));
            }
        }
        // Mark existing and added fields as optional
        for (NSField nsField : nsRecord.getFields()) {
            nsField.setOptional(optional);
        }
        return nsRecord;
    }

    private int getJavaWrapperType(ParameterType parameterType) {
        Class javaClass = parameterType.getType();
        int javaWrapperType = JavaWrapperType.JAVA_TYPE_UNKNOWN;
        if (javaClass.equals(Boolean.class)) {
            javaWrapperType = JavaWrapperType.JAVA_TYPE_BOOLEAN;
        } else if (javaClass.equals(BigInteger.class)) {
            javaWrapperType = JavaWrapperType.JAVA_TYPE_BIG_INTEGER;
        } else if (javaClass.equals(byte[].class)) {
            javaWrapperType = JavaWrapperType.JAVA_TYPE_byte_ARRAY;
        }
        return javaWrapperType;
    }
}