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
import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.NodeFactory;
import com.wm.app.b2b.server.NodeMaster;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.app.b2b.server.dispatcher.Dispatcher;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.app.b2b.server.dispatcher.wmmessaging.Message;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.lang.flow.FlowInvoke;
import com.wm.lang.flow.FlowRoot;
import com.wm.lang.ns.EventDescription;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSRecordUtil;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSSignature;
import com.wm.lang.ns.NSTrigger;
import com.wm.msg.Header;
import com.wm.util.JavaWrapperType;
import com.wm.util.Name;
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

    public static Util instance = new Util();

    private Package pkgWmDApp = PackageManager.getPackage("WmDApp");
    private Package pkgWmDAppContract = PackageManager.getPackage("WmDAppContract");
    private ContractRegistry contractRegistry;
    private Map<String,Contract> contracts;
    private Map<NSName,FlowSvcImpl> services;
    private Map<Trigger,NSRecord> triggers;

    public Web3Service web3Service;

    /**
     * The default constructor.
     * @throws ExceptionInInitializerError if the node configuration is missing
     */
    private Util() throws ExceptionInInitializerError {
        services = new HashMap<>();
        triggers = new HashMap<>();
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
        String functionName = getContractFunction(nsName);
        Contract contract = validate(contracts.get(uri));
        ContractInterface.Specification<?> function = contract.getAbi().getFunctions().stream().filter(o -> o.getName().equals(functionName)).findFirst().orElse(null);
        assert function != null;
        Types.RequestEthCall request = Types.RequestEthCall.newBuilder().setTx(
            Types.TxType.newBuilder().setTo(HexValue.toByteString(contract.getContractAddress())).setData(HexValue.toByteString(encodeInput(function, pipeline))).build()
        ).build();
        Types.ResponseEthCall response = web3Service.ethCall(request);
        decodeOutput(function, pipeline, HexValue.toString(response.getReturn()));
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, functionName, contract.getContractAddress()});
    }

    /**
     * @param nsName The contract's function ns name
     * @param pipeline The input pipeline
     */
    public void sendTransaction(NSName nsName, IData pipeline) throws IOException {
        String uri = getContractUri(nsName);
        String functionName = getContractFunction(nsName);
        Contract contract = validate(contracts.get(uri));
        ContractInterface.Specification<?> function = contract.getAbi().getFunctions().stream().filter(o -> o.getName().equals(functionName)).findFirst().orElse(null);
        assert function != null;
        contract = sendTransaction(contract, encodeInput(function, pipeline));
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, functionName, contract.getContractAddress()});
    }

    /**
     * @param nsName The contract's filter ns name
     * @return the corresponding log observable
     */
    public Observable<Types.FilterLogType> getLogObservable(NSName nsName) throws IOException {
        String uri = getContractUri(nsName);
        Contract contract = validate(contracts.get(uri));
        Types.RequestEthNewFilter requestEthNewFilter = Types.RequestEthNewFilter.newBuilder().setOptions(
            Types.FilterOptionType.newBuilder().setAddress(HexValue.toByteString(contract.getContractAddress())).build()
        ).build();
        Observable<Types.FilterLogType> ethLogObservable = web3Service.ethLogObservable(requestEthNewFilter);
        DAppLogger.logInfo(DAppMsgBundle.DAPP_OBSERVABLE_LOG, new Object[]{uri, contract.getContractAddress()});
        return ethLogObservable;
    }

    /**
     * @param nsName The contract's event ns name
     * @return the data pipeline wrapped as a {@link Message}
     */
    public Message<Types.FilterLogType> decodeLogEvent(NSName nsName, Types.FilterLogType logEvent) throws IOException {
        String uri = getContractUri(nsName);
        String eventName = getContractEvent(nsName);
        Contract contract = validate(contracts.get(uri));
        ContractInterface.Specification<?> event = contract.getAbi().getEvents().stream().filter(o -> o.getName().equals(eventName)).findFirst().orElse(null);
        assert event != null;
        IData pipeline = IDataFactory.create();
        IData envelope = IDataFactory.create();
        String uuid = HexValue.toString(logEvent.getTransactionIndex());
        new IDataMap(envelope).put("uuid", uuid);
        new IDataMap(pipeline).put(Dispatcher.ENVELOPE_KEY, envelope);
        decodeOutput(event, pipeline, HexValue.toString(logEvent.getData()));
        DAppLogger.logInfo(DAppMsgBundle.DAPP_EVENT_LOG, new Object[]{uri, eventName, contract.getContractAddress()});
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
            contract = sendTransaction(contract, contract.getBinary());
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
     * @return the contract functions as a {@link NSName}/{@link FlowSvcImpl} map
     */
    public Map<NSName,FlowSvcImpl> getFunctions() throws IOException {
        contracts = contractRegistry.load();
        for (Map.Entry<String,Contract> entry : contracts.entrySet()) {
            // Add the functions as defined in the ABI
            String interfaceName = getInterfaceName(entry.getKey());
            ContractInterface contractInterface = entry.getValue().getAbi();
            List<ContractInterface.Specification> functions = contractInterface.getFunctions();
            for (ContractInterface.Specification<?> function : functions) {
                String functionName = function.getName() + SUFFIX_REQ;
                NSName nsName = NSName.create(interfaceName, functionName);
                NSSignature nsSignature = getSignature(nsName, function);
                FlowInvoke flowInvoke = new FlowInvoke(IDataFactory.create());
                FlowSvcImpl flowSvcImpl;
                if (function.isConstant()) {
                    flowInvoke.setService(NSName.create("wm.dapp.Contract:call"));
                    flowSvcImpl = getFlowSvcImpl(nsName, nsSignature, flowInvoke);
                    flowSvcImpl.setStateless(true);
                } else {
                    flowInvoke.setService(NSName.create("wm.dapp.Contract:sendTransaction"));
                    flowSvcImpl = getFlowSvcImpl(nsName, nsSignature, flowInvoke);
                    flowSvcImpl.setStateless(false);
                }
                services.put(nsName, flowSvcImpl);
            }
        }
        return services;
    }

    /**
     * @return the contract events as a {@link Trigger}/{@link NSRecord} map
     */
    public Map<Trigger,NSRecord> getEvents() throws IOException {
        contracts = contractRegistry.load();
        for (Map.Entry<String,Contract> entry : contracts.entrySet()) {
            // Add the events as defined in the ABI
            String interfaceName = getInterfaceName(entry.getKey());
            ContractInterface contractInterface = entry.getValue().getAbi();
            List<ContractInterface.Specification> events = contractInterface.getEvents();
            // Response service
            String serviceName = "pub.flow:debugLog";
            NSName svcNsName = NSName.create(serviceName);
            // Remember all record ns nodes for this contract
            Map<NSName,NSRecord> nsRecords = new HashMap<>();
            for (ContractInterface.Specification<?> event : events) {
                // The record name
                NSName pdtNsName = NSName.create(interfaceName, event.getName() + SUFFIX_DOC);
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
                triggers.put(trigger, pdt);
            }
        }
        return triggers;
    }

    public NSRecord getPublishableDocumentType(NSName nsName) {
        EventDescription eventDescription = EventDescription.create(IS_DAPP_CONNECTION, Name.create(nsName.getFullName()), 0, EventDescription.VOLATILE);
        NSRecord nsRecord = new NSRecord(Namespace.current(), nsName.getFullName(), NSRecord.DIM_SCALAR);
        nsRecord.setNSName(nsName);
        nsRecord.setPackage(pkgWmDAppContract);
//        IData result = Transformer.transform(Namespace.current(), nsRecord.getNSName().getFullName(), 0, EventDescription.VOLATILE, false, IS_DAPP_CONNECTION, false, false);
//        assert result.equals(IDataFactory.create(new Object[][]{{"isSuccessful","true"}}));
        NSRecordUtil.transform(nsRecord, eventDescription);
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
        IDataMap pipe = new IDataMap(pipeline);
        List<T> values = new ArrayList<>();
        List<? extends ContractInterface.Parameter<T>> inputParameters = function.getInputParameters();
        for (ContractInterface.Parameter<T> parameter : inputParameters) {
            ParameterType<T> parameterType = parameter.getType();
            T value = parameterType.asType(pipe.get(parameter.getName()));
            values.add(value);
        }
        return function.encode(values);
    }

    private <T> void decodeOutput(ContractInterface.Specification<T> specification, IData pipeline, String response) {
        List<T> values = specification.decode(response);
        List<? extends ContractInterface.Parameter<T>> parameters = "event".equals(specification.getType()) ? specification.getInputParameters() : specification.getOutputParameters();
        assert values.size() == parameters.size();
        Iterator<? extends ContractInterface.Parameter<T>> outputParametersIterator = parameters.iterator();
        Iterator<T> valuesIterator = values.iterator();
        while (outputParametersIterator.hasNext() && valuesIterator.hasNext()) {
            new IDataMap(pipeline).put(outputParametersIterator.next().getName(), valuesIterator.next());
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

    private Contract sendTransaction(Contract contract, String data) throws IOException {
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
        return contract;
    }

    private String getInterfaceName(String uri) {
        return uri.replace('/', '.');
    }

    private String getContractUri(NSName nsName) {
        return nsName.getInterfaceName().toString().replace('.', '/');
    }

    private String getContractFunction(NSName nsName) {
        String name = nsName.getNodeName().toString();
        return name.endsWith(SUFFIX_REQ) ? name.substring(0, name.length() - SUFFIX_REQ.length()) : name;
    }

    private String getContractEvent(NSName nsName) {
        String name = nsName.getNodeName().toString();
        return name.endsWith(SUFFIX_DOC) ? name.substring(0, name.length() - SUFFIX_DOC.length()) : name;
    }

    private FlowSvcImpl getFlowSvcImpl(NSName nsName, NSSignature nsSignature, FlowInvoke flowInvoke) {
        mkdirs(nsName);

        FlowSvcImpl flowSvcImpl;
        NSNode node = Namespace.current().getNode(nsName);
        if (node instanceof FlowSvcImpl) {
            flowSvcImpl = (FlowSvcImpl) node;
        } else {
            flowSvcImpl = new FlowSvcImpl(pkgWmDAppContract, nsName, null);
            flowSvcImpl.setServiceSigtype(NSService.SIG_JAVA_3_5);
            flowSvcImpl.setFlowRoot(new FlowRoot(IDataFactory.create()));
            flowSvcImpl.getServiceType().setSubtype("dapp"); // TODO :: Maybe add global field to NSServiceType.SVCSUB_DAPP
        }

        if (nsSignature != null) {
            flowSvcImpl.setSignature(nsSignature);
        } else {
            flowSvcImpl.setSignature(new NSSignature(new NSRecord(Namespace.current()), new NSRecord(Namespace.current())));
        }


        flowSvcImpl.getFlowRoot().addNode(flowInvoke);
        return flowSvcImpl;
    }

    private <T> NSSignature getSignature(NSName nsName, ContractInterface.Specification<T> function) {
        // If the same ns node with a different signature already exists we simply add to the existing signature ...
        NSSignature nsSignature = services.containsKey(nsName) ? services.get(nsName).getSignature() : NSSignature.create(Namespace.current(), IDataFactory.create());
        // ... but make the parameters optional
        boolean optional = services.containsKey(nsName);

        // Input
        NSRecord inputRecord = getNsRecord(function.getInputParameters(), optional);
        nsSignature.setInput(inputRecord);

        // Output
        NSRecord outputRecord = getNsRecord(function.getOutputParameters(), optional);
        nsSignature.setOutput(outputRecord);

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