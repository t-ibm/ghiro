/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.conf.Node;
import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.contract.ContractRegistry;
import com.softwareag.tom.contract.SolidityLocationFileSystem;
import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.contract.abi.ParameterType;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IDataFactory;
import com.wm.lang.flow.FlowInvoke;
import com.wm.lang.flow.FlowRoot;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSSignature;
import com.wm.util.JavaWrapperType;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Util {
    private Package pkg = PackageManager.getPackage("WmDApp");
    private Map<NSName,FlowSvcImpl> nsNodes;
    private Util() { nsNodes = new HashMap<>(); }
    public static Util create() { return new Util(); }

    /**
     * @return the contract functions as a {@link NSName}/{@link FlowSvcImpl} map
     * @throws IOException if the contracts cannot be loaded from the registry
     */
    public Map<NSName,FlowSvcImpl> getFunctions() throws IOException {
        System.setProperty(Node.SYSTEM_PROPERTY_TOMCONFNODE, pkg == null ? "default" : String.valueOf(pkg.getManifest().getProperty("node")));
        Map<String, Contract> contracts;
        File contractRegistryLocation = new File(Node.instance().getContract().getRegistry().getLocation().getPath());
        ContractRegistry contractRegistry = ContractRegistry.build(new SolidityLocationFileSystem(contractRegistryLocation));
        contracts = contractRegistry.load();
        NSName nsName;
        NSSignature nsSignature;
        FlowInvoke flowInvoke;
        FlowSvcImpl flowSvcImpl;
        for (Map.Entry<String, Contract> entry : contracts.entrySet()) {
            // Add the functions as defined in the ABI
            String folderName = entry.getKey().replaceAll("/", ".");
            ContractInterface contractInterface = entry.getValue().getAbi();
            List<ContractInterface.Specification> functions = contractInterface.getFunctions();
            for (ContractInterface.Specification<?> function : functions) {
                String functionName = function.getName();
                nsName = NSName.create(folderName, functionName);
                nsSignature = getSignature(nsName, function);
                flowInvoke = new FlowInvoke(IDataFactory.create());
                if (function.isConstant()) {
                    flowInvoke.setService(NSName.create("wm.dapp.Contract:call"));
                    flowSvcImpl = getFlowSvcImpl(nsName, nsSignature, flowInvoke);
                    flowSvcImpl.setStateless(true);
                } else {
                    flowInvoke.setService(NSName.create("wm.dapp.Contract:sendTransaction"));
                    flowSvcImpl = getFlowSvcImpl(nsName, nsSignature, flowInvoke);
                    flowSvcImpl.setStateless(false);
                }
                nsNodes.put(nsName, flowSvcImpl);
            }
            // Add the deploy service
            nsName = NSName.create(folderName, "deploy");
            nsSignature = getSignatureDeploy();
            flowInvoke = new FlowInvoke(IDataFactory.create());
            flowInvoke.setService(NSName.create("wm.dapp.Contract:sendTransaction"));
            flowSvcImpl = getFlowSvcImpl(nsName, nsSignature, flowInvoke);
            flowSvcImpl.setStateless(false);
            nsNodes.put(nsName, flowSvcImpl);
            // Add the load service
            nsName = NSName.create(folderName, "load");
            nsSignature = getSignatureLoad();
            flowInvoke = new FlowInvoke(IDataFactory.create());
            flowInvoke.setService(NSName.create("wm.dapp.Contract:load"));
            flowSvcImpl = getFlowSvcImpl(nsName, nsSignature, flowInvoke);
            flowSvcImpl.setStateless(true);
            nsNodes.put(nsName, flowSvcImpl);
        }
        return nsNodes;
    }

    private FlowSvcImpl getFlowSvcImpl(NSName nsName, NSSignature nsSignature, FlowInvoke flowInvoke) {
        if (pkg != null && !pkg.getStore().getNodePath(nsName).mkdirs()) {
            DAppLogger.logDebug(DAppMsgBundle.DAPP_SERVICES_MKDIRS, new Object[]{"" + nsName});
        }

        FlowSvcImpl flowSvcImpl;
        NSNode node = Namespace.current().getNode(nsName);
        if (node != null && node instanceof FlowSvcImpl) {
            flowSvcImpl = (FlowSvcImpl) node;
        } else {
            flowSvcImpl = new FlowSvcImpl(pkg, nsName,null);
            flowSvcImpl.setServiceSigtype(NSService.SIG_JAVA_3_5);
            flowSvcImpl.setFlowRoot(new FlowRoot(IDataFactory.create()));
            flowSvcImpl.getServiceType().setSubtype("default"); // TODO :: Maybe add global field to NSServiceType.SVCSUB_DAPP
        }

        if (nsSignature != null) {
            flowSvcImpl.setSignature(nsSignature);
        } else {
            flowSvcImpl.setSignature(new NSSignature(new NSRecord(Namespace.current()), new NSRecord(Namespace.current())));
        }


        flowSvcImpl.getFlowRoot().addNode(flowInvoke);
        return flowSvcImpl;
    }

    private NSSignature getSignatureDeploy() {
        NSSignature nsSignature = NSSignature.create(Namespace.current(), IDataFactory.create());
        NSRecord outputRecord = new NSRecord(Namespace.current());
        nsSignature.setOutput(outputRecord);

        NSRecord txReceipt = (NSRecord) outputRecord.addField("txReceipt", NSField.FIELD_RECORD, NSField.DIM_SCALAR);
        txReceipt.addField("transactionHash", NSField.FIELD_STRING, NSField.DIM_SCALAR);
        txReceipt.addField("contractAddress", NSField.FIELD_STRING, NSField.DIM_SCALAR);

        return nsSignature;
    }

    private NSSignature getSignatureLoad() {
        NSSignature nsSignature = NSSignature.create(Namespace.current(), IDataFactory.create());
        NSRecord inputRecord = new NSRecord(Namespace.current());
        nsSignature.setInput(inputRecord);

        inputRecord.addField("contractAddress", NSField.FIELD_STRING, NSField.DIM_SCALAR);

        return  nsSignature;
    }

    private <T> NSSignature getSignature(NSName nsName, ContractInterface.Specification<T> function) {
        // If the same ns node with a different signature already exists we simply add to the existing signature ...
        NSSignature nsSignature = nsNodes.containsKey(nsName) ? nsNodes.get(nsName).getSignature() : NSSignature.create(Namespace.current(), IDataFactory.create());
        // ... but make the parameters optional
        boolean optional = nsNodes.containsKey(nsName);

        // Input
        NSRecord inputRecord = getNsRecord(function.getInputParameters(), optional);
        nsSignature.setInput(inputRecord);

        // Output
        NSRecord outputRecord = getNsRecord(function.getOutputParameters(), optional);
        nsSignature.setOutput(outputRecord);

        return nsSignature;
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
        return  javaWrapperType;
    }
}