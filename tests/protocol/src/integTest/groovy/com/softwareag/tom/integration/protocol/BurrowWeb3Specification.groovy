/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.protocol


import com.softwareag.tom.util.HexValueBase
import groovyx.net.http.RESTClient

/**
 * System under specification: Burrow Web3 endpoints.
 * @author tglaeser
 */
class BurrowWeb3Specification extends RestClientBaseSpecification {

    def setup() {
        given: 'a REST client'
        client = new RESTClient("http://${config.node.host.ip}:${config.node.host.web3.port}")
    }

    def "test 'web3_clientVersion'"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'web3_clientVersion']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.startsWith '0.29.1'
    }

    def "test 'eth_blockNumber'"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'eth_blockNumber']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        HexValueBase.toBigInteger(resp.data.result as String) >= 1
    }

    def "test 'eth_getBlockByNumber'"() {
        given: 'a valid JSON-RPC request'
        BigInteger height = 1
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'eth_getBlockByNumber', 'params': [height as String,false]]

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.transactions.size() == 0
        HexValueBase.toBigInteger(resp.data.result.number as String) == height
    }

    def "test 'eth_pendingTransactions'"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'eth_pendingTransactions']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.txs == []
    }
}