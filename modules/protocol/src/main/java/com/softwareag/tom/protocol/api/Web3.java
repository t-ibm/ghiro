/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.api;

import com.softwareag.tom.protocol.abi.Types;

/**
 * The Ethereum JSON-RPC API. See the <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC">JSON-RPC Wiki</a> for more info.
 */
public interface Web3 extends Net, Eth {
    /**
     * Method {@code web3_clientversion}.
     * @param req An empty request
     * @return the current client version
     */
    Types.ResponseWeb3ClientVersion web3ClientVersion(Types.RequestWeb3ClientVersion req);
}