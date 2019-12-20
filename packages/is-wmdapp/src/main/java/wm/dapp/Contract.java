/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package wm.dapp;

// --- <<IS-START-IMPORTS>> ---
import com.softwareag.tom.is.pkg.dapp.Util;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.lang.ns.NSName;

import java.io.IOException;
// --- <<IS-END-IMPORTS>> ---

public final class Contract {
    static Util util = Util.instance();
    Contract() {}
    /**
     * Calls the contract. To be used by stateless EVM contracts.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void call(IData pipeline) throws ServiceException {
        // --- <<IS-START(call)>> ---
        // @subtype unknown
        // @sigtype java 3.5
        NSName nsName = NSName.create(InvokeState.getCurrentState().getFlowState().current().getFlowRoot().getNSName());
        try {
            util.web3().runContract(nsName, pipeline, false);
        } catch (IOException e) {
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }

    /**
     * Sends a transaction. To be used by stateful EVM contracts.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void sendTransaction(IData pipeline) throws ServiceException {
        // --- <<IS-START(sendTransaction)>> ---
        // @sigtype java 3.5
        NSName nsName = NSName.create(InvokeState.getCurrentState().getFlowState().current().getFlowRoot().getNSName());
        try {
            util.web3().runContract(nsName, pipeline, true);
        } catch (IOException e) {
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }
}