/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.protocol

import groovyx.net.http.RESTClient

/**
 * System under specification: Burrow endpoints.
 * @author tglaeser
 */
class BurrowTest extends RestClientSpecification {

    def setup() {
        given: 'a REST client'
        client = new RESTClient("http://${config.node.host.ip}:${config.node.host.port}")
    }

    def "test 'getClientVersion' via http"() {
        given: 'a valid HTTP request'
        def request = '/network/client_version'

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data == ['client_version': '0.8.0']
    }

    def "test 'getClientVersion' via rpc"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.getClientVersion']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.client_version == '0.8.0'
    }

    def "test 'getValidators' via rpc"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.getValidators']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        List result = resp.data.result.pop()
        result.get(1).validator.accum == 0
        result.get(1).validator.address == '41B19CDD0BFE829B6BA1B69855E4F2FB581198F1'
        result.get(1).validator.pub_key.get(1) == 'E09EB61982E02EA455216072F5D9AD9EE6CB2B9385EDCF1F73CEEBCA7C634C78'
        result.get(1).validator.voting_power == 1000000
    }

    def "test 'getLatestBlock' via rpc"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.getLatestBlock']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.data.txs == []
        resp.data.result.header.chain_id == 'MyChainId'
        resp.data.result.header.height > 1
        resp.data.result.header.num_txs == 0
        resp.data.result.last_commit.blockID.hash == resp.data.result.header.last_block_id.hash
    }

    def "test 'getBlock' via rpc"() {
        given: 'a valid JSON-RPC request'
        def height = 1
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.getBlock', 'params': ['height':height]]

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.data.txs.size() == resp.data.result.header.num_txs
        resp.data.result.header.chain_id == 'MyChainId'
        resp.data.result.header.height == height
        resp.data.result.last_commit.blockID.hash == resp.data.result.header.last_block_id.hash
    }

    def "test 'getUnconfirmedTxs' via rpc"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.getUnconfirmedTxs']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.txs == []
    }

    def "test 'getAccount' via rpc"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.getAccount', 'params': ['address':'F60D30722E7B497FA532FB3207C3FB29C31B1992']]

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.address == 'F60D30722E7B497FA532FB3207C3FB29C31B1992'
        resp.data.result.balance == 200000000
        resp.data.result.code == ''
        resp.data.result.pub_key == null
        resp.data.result.sequence == 0
        resp.data.result.storage_root == ''
        resp.data.result.permissions.base.perms == 2302
        resp.data.result.permissions.base.set == 16383
    }

    def "test create_and_solidity_event via rpc"() {
        given: 'a valid Solidity contract'
        String contract = '6060604052608f8060106000396000f360606040523615600d57600d565b608d5b7f68616861000000000000000000000000000000000000000000000000000000007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f88c4f556fdc50387ec6b6fc4e8250fecc56ff50e873df06dadeeb84c0287ca9060016040518082815260200191505060405180910390a35b565b00'
        def caller = [
                address:'37236DF251AB70022B1DA351F08A20FB52443E37',
                pub_key:'CB3688B7561D488A2A4834E1AEE9398BEF94844D8BDBBCA980C11E3654A45906',
                priv_key:'6B72D45EB65F619F11CE580C8CAED9E0BADC774E9C9C334687A65DCBAD2C4151CB3688B7561D488A2A4834E1AEE9398BEF94844D8BDBBCA980C11E3654A45906'
        ]
        def params = [
                'priv_key':caller.priv_key,
                'data':contract,
                'address':'',
                'fee':12,
                'gas_limit':223,
        ]
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.transactAndHold', 'params': params]

        when: println '(1) the transaction is fully processed'
        resp = send request
        def callee = resp.data.result.call_data.callee
        def ret = resp.data.result.return

        then: 'a valid response is received'
        resp.data.result.exception == ''
        resp.data.result.origin == caller.address
        resp.data.result.call_data.caller == caller.address

        when: println '(2) the newly created contract account is verified'
        request = ['id': '2', 'jsonrpc': '2.0', 'method': 'burrow.getAccount', 'params': ['address':callee]]
        resp = send request
        def storageRoot = resp.data.result.storage_root

        then: 'a valid response is received'
        resp.data.result.address == callee
        resp.data.result.balance == 0
        resp.data.result.code == ret
        resp.data.result.sequence == 0
        resp.data.result.permissions.base.perms == 2302
        resp.data.result.permissions.base.set == 16383

        when: println '(3) the complete storage of the contract account is retrieved'
        request = ['id': '3', 'jsonrpc': '2.0', 'method': 'burrow.getStorage', 'params': ['address':callee]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.storage_root == storageRoot
        resp.data.result.storage_items == []

        when: println '(4) we subscribe to events from the the new contract account'
        String eventId = "Log/$callee"
        request = ['id': '4', 'jsonrpc': '2.0', 'method': 'burrow.eventSubscribe', 'params': ['event_id':eventId]]
        resp = send request
        def subId = resp.data.result.sub_id

        and: println '(5) poll for events'
        request = ['id': '5', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':subId]]
        resp = send request

        then: 'no events exist'
        resp.data.result.events == []

        when: println '(6) the contract is executed 3 times'
        3.times {
            request = ['id': '6', 'jsonrpc': '2.0', 'method': 'burrow.call', 'params': ['address':callee, 'data':'']]
            resp = send request
        }

        then: 'a valid response is received'
        resp.data.result.gas_used == 49

        when: println '(7) we poll for events again'
        request = ['id': '7', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':subId]]
        resp = send request

        then: 'an event for each call is received'
        resp.data.result.events.size() == 3

        when: println '(8) we poll for events again'
        request = ['id': '8', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':subId]]
        resp = send request

        then: 'no events exist'
        resp.data.result.events.size() == 0
    }
}