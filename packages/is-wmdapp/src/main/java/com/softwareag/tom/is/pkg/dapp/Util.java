/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.contract.abi.ParameterType;
import com.softwareag.tom.is.pkg.dapp.trigger.Condition;
import com.wm.app.b2b.broker.conv.Transformer;
import com.wm.app.b2b.broker.conv.TypeCoderException;
import com.wm.app.b2b.broker.sync.SyncException;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.NodeFactory;
import com.wm.app.b2b.server.NodeMaster;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.app.b2b.server.dispatcher.DispatchFacade;
import com.wm.app.b2b.server.dispatcher.exceptions.MessagingSubsystemException;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.app.b2b.server.dispatcher.wmmessaging.ConnectionAlias;
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
import com.wm.lang.ns.NSRecordRef;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSServiceType;
import com.wm.lang.ns.NSSignature;
import com.wm.lang.ns.NSTrigger;
import com.wm.msg.ICondition;
import com.wm.util.JavaWrapperType;
import com.wm.util.Values;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.softwareag.tom.is.pkg.dapp.trigger.DAppListenerBase.IS_DAPP_CONNECTION;

public class Util extends UtilBase<NSName> {
    static final String SUFFIX_REQ = "Req";
    static final String SUFFIX_DOC = "Doc";
    static final String SUFFIX_REP = "Rep";

    private static Util instance;
    public RuntimeConfiguration rt;

    private static Package pkgWmDApp = PackageManager.getPackage("WmDApp");
    private static Package pkgWmDAppContract = PackageManager.getPackage("WmDAppContract");
    private Map<String,FlowSvcImpl> functions;
    private Map<String,Event> events;

    /**
     * The default constructor.
     * @throws ExceptionInInitializerError if the node configuration is missing
     */
    public Util(String nodeName) throws ExceptionInInitializerError {
        super(nodeName);
        functions = new HashMap<>();
        events = new HashMap<>();
    }

    /**
     * @return an instance as a singleton
     */
    public static Util instance() {
        if (instance == null) {
            instance = new Util(pkgWmDApp == null ? "default" : String.valueOf(pkgWmDApp.getManifest().getProperty("node")));
        }
        return instance;
    }

    @Override public String getContractUri(NSName nsName) {
        return nsName.getInterfaceName().toString().replace('.', '/');
    }

    @Override public String getFunctionUri(NSName nsName) {
        String name = nsName.getNodeName().toString();
        return name.endsWith(SUFFIX_REQ) ? name.substring(0, name.length() - SUFFIX_REQ.length()) : name;
    }

    @Override public String getEventUri(NSName nsName) {
        String name = nsName.getNodeName().toString();
        return name.endsWith(SUFFIX_DOC) ? name.substring(0, name.length() - SUFFIX_DOC.length()) : name;
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
        loadContracts();
        return getContract(uri).getContractAddress() != null;
    }

    /**
     * @return the contracts-address mapping as a {@link IData} list with fields {@code uri} and {@code contractAddress}
     */
    public IData[] getContractAddresses() throws IOException {
        return loadContracts().entrySet().stream().map(entry -> IDataFactory.create(new Object[][]{
            {"uri", entry.getKey()},
            {"address", entry.getValue().getContractAddress()},
        })).toArray(IData[]::new);
    }

    /**
     * @param deployedOnly If set to {@code true} returns only functions from deployed contracts, otherwise returns all defined
     * @return the contract functions as a {@link NSName}/{@link FlowSvcImpl} map
     */
    public Map<String,FlowSvcImpl> getFunctions(boolean deployedOnly) throws IOException {
        for (Map.Entry<String,Contract> entry : loadContracts(deployedOnly).entrySet()) {
            // Add the functions as defined in the ABI
            String interfaceName = getInterfaceName(entry.getKey());
            List<ContractInterface.Specification> functions = entry.getValue().getAbi().getFunctions();
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
        for (Map.Entry<String,Contract> entry : loadContracts(deployedOnly).entrySet()) {
            // Add the events as defined in the ABI
            String interfaceName = getInterfaceName(entry.getKey());
            List<ContractInterface.Specification> events = entry.getValue().getAbi().getEvents();
            // The trigger
            NSName triggerNsName = NSName.create(interfaceName, "trigger");
            Trigger trigger = createTrigger(triggerNsName);
            // Remember all record ns nodes for this contract
            Map<NSName,NSRecord> nsRecords = new HashMap<>();
            for (ContractInterface.Specification<?> event : events) {
                String eventName = interfaceName + ':' + event.getName();
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
                NSSignature nsSignature = getEventSignature(eventName, pdt);
                FlowSvcImpl flowSvcImpl = createFlowSvcImpl(svcNsName, nsSignature, flowInvoke, NSServiceType.create(NSServiceType.SVC_FLOW,NSServiceType.SVCSUB_UNKNOWN));
                // Add condition
                addCondition(trigger, Condition.create(pdtNsName, svcNsName).asCondition());
                // Add to the event condition map
                this.events.put(eventName, Event.create(trigger, pdt, flowSvcImpl));
            }
        }
        return events;
    }

    public List<Trigger> getTriggers(Map<String,Event> events) {
        return events.values().stream().map(Event::getTrigger).distinct().collect(Collectors.toList());
    }

    public ConnectionAlias getConnectionAlias() {
        rt = rt != null ? rt : DispatchFacade.getRuntimeConfiguration();
        try {
            return rt.getConnectionAlias(IS_DAPP_CONNECTION);
        } catch (MessagingSubsystemException e) {
            return null;
        }
    }

    public void createConnectionAlias() throws Exception {
        rt = rt != null ? rt : DispatchFacade.getRuntimeConfiguration();

        IData input = IDataFactory.create();
        IDataCursor ic = input.getCursor();
        IDataUtil.put(ic, "aliasName", IS_DAPP_CONNECTION);
        IDataUtil.put(ic, "description", "system generated Decentralised Application connection alias");
        IDataUtil.put(ic, "enabled", true);
        IDataUtil.put(ic, "systemGenerated", true);
        ic.destroy();

        rt.createDAppConnectionAliasReference(input);
    }

    public NSRecord getPublishableDocumentType(NSName nsName) throws SyncException, TypeCoderException {
        ConnectionAlias alias = getConnectionAlias();
        assert alias != null;
        NSRecord nsRecord = new NSRecord(Namespace.current(), nsName.getFullName(), NSRecord.DIM_SCALAR);
        nsRecord.setNSName(nsName);
        nsRecord.setPackage(pkgWmDAppContract);
        IData result = Transformer.transformTo(Namespace.current(), nsRecord, 0, EventDescription.VOLATILE, false, alias, IS_DAPP_CONNECTION, false, false);
        assert result.equals(IDataFactory.create(new Object[][]{{"isSuccessful","true"}}));
//        EventDescription eventDescription = EventDescription.create(IS_DAPP_CONNECTION, Name.create(nsName.getFullName()), 0, EventDescription.VOLATILE);
//        NSRecordUtil.transform(nsRecord, eventDescription);
        return nsRecord;
    }

    public static FlowSvcImpl getResponseService(NSName nsName) {
        return new FlowSvcImpl(pkgWmDAppContract, nsName, null);
    }

    public static Trigger createTrigger(NSName nsName) {
        NodeFactory nf = NodeMaster.getFactory(NSTrigger.TYPE.getType());
        IData nodeDef = IDataFactory.create(new Object[][]{
            {NSNode.KEY_NSN_NSNAME, nsName.getFullName()},
            {NSNode.KEY_NSN_TYPE, NSTrigger.TYPE_KEY},
        });
        Trigger trigger = (Trigger)nf.createFromNodeDef(pkgWmDAppContract, nsName, Values.use(nodeDef));
        trigger.setPackage(pkgWmDAppContract);
        return trigger;
    }

    public static void addCondition(Trigger trigger, ICondition triggerCondition) {
        ICondition[] c = trigger.getConditions() != null ? trigger.getConditions() : new ICondition[]{};
        List<ICondition> triggerConditions = new ArrayList<>(Arrays.asList(c));
        triggerConditions.add(triggerCondition);
        trigger.setConditions(triggerConditions.toArray(c));
    }

    private static String getInterfaceName(String uri) {
        return uri.replace('/', '.');
    }

    private static FlowSvcImpl createFlowSvcImpl(NSName nsName, NSSignature nsSignature, FlowInvoke flowInvoke, NSServiceType serviceType) {
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

    private NSSignature getEventSignature(String eventName, NSRecord pdt) {
        // If the same ns node with a different signature already exists we simply add to the existing signature ...
        NSSignature nsSignature = events.containsKey(eventName) ? events.get(eventName).getService().getSignature() : NSSignature.create(Namespace.current(), IDataFactory.create());
        // ... but make the parameters optional
        boolean optional = events.containsKey(eventName);

        // Pdt reference
        NSRecordRef pdtRef = new NSRecordRef(Namespace.current(), pdt.getName(), pdt, NSField.DIM_SCALAR);
        pdtRef.setOptional(optional);

        // Input
        NSRecord inputRecord = nsSignature.getInput() != null ? nsSignature.getInput() : new NSRecord(Namespace.current());
        if (inputRecord.getField(pdt.getName(), NSField.FIELD_RECORDREF, NSField.DIM_SCALAR) == null) {
            inputRecord.addField(pdtRef);
        }
        nsSignature.setInput(inputRecord);

        return nsSignature;
    }

    private static void mkdirs(NSName nsName) {
        if (pkgWmDAppContract != null && !pkgWmDAppContract.getStore().getNodePath(nsName).mkdirs()) {
            DAppLogger.logDebug(DAppMsgBundle.DAPP_SERVICES_MKDIRS, new Object[]{"" + nsName});
        }
    }

    private static <T> NSRecord getNsRecord(List<? extends ContractInterface.Parameter<T>> parameters, boolean optional) {
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

    private static int getJavaWrapperType(ParameterType parameterType) {
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