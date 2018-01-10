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
import com.softwareag.tom.protocol.Web3Service;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.ServiceHttp;
import com.softwareag.tom.protocol.util.HexValue;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IData;
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
import java.util.stream.Collectors;

public enum Util {
    instance;

    private Package pkg = PackageManager.getPackage("WmDApp");
    private ContractRegistry contractRegistry;
    private Map<String,Contract> contracts;
    private Map<NSName,FlowSvcImpl> nsNodes;
    private Web3Service web3Service;

    Util() throws ExceptionInInitializerError {
        nsNodes = new HashMap<>();
        System.setProperty(Node.SYSTEM_PROPERTY_TOMCONFNODE, pkg == null ? "default" : String.valueOf(pkg.getManifest().getProperty("node")));
        try {
            File contractRegistryLocation = new File(Node.instance().getContract().getRegistry().getLocation().getPath());
            File configLocation = new File(Node.instance().getConfig().getLocation().getPath());
            contractRegistry = ContractRegistry.build(new SolidityLocationFileSystem(contractRegistryLocation), new ConfigLocationFileSystem(configLocation));
            web3Service = Web3Service.build(new ServiceHttp("http://" + Node.instance().getHost().getIp() +':' + Node.instance().getHost().getPort() + "/rpc"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * @param nsName The contract functions ns name
     * @param pipeline The input pipeline
     */
    public void call(NSName nsName, IData pipeline) {
        String uri = getContractUri(nsName);
        String functionName = getContractFunction(nsName);
        Contract contract = validate(contracts.get(uri));
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, functionName, contract.getContractAddress()});
    }

    /**
     * @param nsName The contract functions ns name
     * @param pipeline The input pipeline
     */
    public void sendTransaction(NSName nsName, IData pipeline) {
        String uri = getContractUri(nsName);
        String functionName = getContractFunction(nsName);
        Contract contract = validate(contracts.get(uri));
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_CALL, new Object[]{uri, functionName, contract.getContractAddress()});
    }

    public String deployContract(String uri) {
        Contract contract = contracts.get(uri);
        String contractAddress;
        if (contract.getContractAddress() != null) {
            throw new IllegalStateException("Contract address not null; it seems the contract was already deployed!");
        } else {
            // eth_sendTransaction
            Types.RequestEthSendTransaction requestEthSendTransaction = Types.RequestEthSendTransaction.newBuilder().setTx(
                    Types.TxType.newBuilder().setData(HexValue.toByteString(contract.getBinary())).setGas(HexValue.toByteString(contract.getGasLimit())).setGasPrice(HexValue.toByteString(contract.getGasPrice())).build()
            ).build();
            Types.ResponseEthSendTransaction responseEthSendTransaction = web3Service.ethSendTransaction(requestEthSendTransaction);
            // eth_getTransactionReceipt
            Types.RequestEthGetTransactionReceipt requestEthGetTransactionReceipt = Types.RequestEthGetTransactionReceipt.newBuilder().setHash(responseEthSendTransaction.getHash()).build();
            Types.ResponseEthGetTransactionReceipt responseEthGetTransactionReceipt = web3Service.ethGetTransactionReceipt(requestEthGetTransactionReceipt);
            contractAddress = HexValue.toString(responseEthGetTransactionReceipt.getTxReceipt().getContractAddress());
        }
        return contractAddress;
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
    public void storeContractAddresse(String uri, String contractAddress) throws IOException {
        DAppLogger.logInfo(DAppMsgBundle.DAPP_CONTRACT_DEPLOY, new Object[]{uri, contractAddress});
        contracts = contractRegistry.load();
        contracts.put(uri, contracts.get(uri).setContractAddress(contractAddress));
        contracts = contractRegistry.storeContractAddresses(contracts);
    }

    /**
     * @return the contract functions as a {@link NSName}/{@link FlowSvcImpl} map
     */
    public Map<NSName,FlowSvcImpl> getFunctions() throws IOException {
        NSName nsName;
        NSSignature nsSignature;
        FlowInvoke flowInvoke;
        FlowSvcImpl flowSvcImpl;
        contracts = contractRegistry.load();
        for (Map.Entry<String, Contract> entry : contracts.entrySet()) {
            // Add the functions as defined in the ABI
            String interfaceName = getInterfaceName(entry.getKey());
            ContractInterface contractInterface = entry.getValue().getAbi();
            List<ContractInterface.Specification> functions = contractInterface.getFunctions();
            for (ContractInterface.Specification<?> function : functions) {
                String functionName = function.getName();
                nsName = NSName.create(interfaceName, functionName);
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
        }
        return nsNodes;
    }

    private Contract validate(Contract contract) throws IllegalStateException {
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

    private String getInterfaceName(String uri) {
        return uri.replace('/', '.');
    }

    private String getContractUri(NSName nsName) {
        return nsName.getInterfaceName().toString().replace('.', '/');
    }

    private String getContractFunction(NSName nsName) {
        return nsName.getNodeName().toString();
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