/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package wm.dapp;

// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
// --- <<IS-END-IMPORTS>> ---

public final class Contract {
    /**
     * Calls the contract.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void call(IData pipeline) throws ServiceException {
        // --- <<IS-START(call)>> ---
        // @subtype unknown
        // @sigtype java 3.5
        // [i] field:0:required data
        // [o] object:0:required return

        // --- <<IS-END>> ---
    }

    /**
     * Loads the contract.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void load(IData pipeline) throws ServiceException {
        // --- <<IS-START(load)>> ---
        // @sigtype java 3.5
        // [i] field:0:required contractAddress

        // --- <<IS-END>> ---
    }

    /**
     * Sends a transaction.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void sendTransaction(IData pipeline) throws ServiceException {
        // --- <<IS-START(sendTransaction)>> ---
        // @sigtype java 3.5
        // [i] field:0:optional contractAddress
        // [i] field:0:required data
        // [o] record:0:required txReceipt
        // [o] - field:0:required transactionHash
        // [o] - field:0:required contractAddress

        // --- <<IS-END>> ---
    }
}