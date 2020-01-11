/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.api;

import com.softwareag.tom.protocol.BurrowService;
import com.softwareag.tom.protocol.grpc.ServiceTransact;
import org.hyperledger.burrow.execution.Exec;
import org.hyperledger.burrow.txs.Payload;

/**
 * The transact portion of the Burrow gRPC API. See the protocol buffers IDL file <a href="https://github.com/hyperledger/burrow/blob/master/protobuf/rpctransact.proto">rpctransact.proto</a> for more info.
 */
public interface BurrowTransact extends BurrowService<ServiceTransact> {

    /**
     * Formulate a {@code Payload.SendTx} transaction signed server-side and wait for it to be included in a block, retrieving response.
     * @param request The request object
     * @return the response
     */
    Exec.TxExecution sendTx(Payload.SendTx request);

    /**
     * Formulate a {@code Payload.CallTx} transaction signed server-side and wait for it to be included in a block, retrieving response.
     * @param request The request object
     * @return the response
     */
    Exec.TxExecution callTx(Payload.CallTx request);
}