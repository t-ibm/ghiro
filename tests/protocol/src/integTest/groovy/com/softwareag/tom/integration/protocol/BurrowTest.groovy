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

    def "test 'genPrivAccount' via rpc"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.genPrivAccount']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.address.length() == 20*2
        resp.data.result.pub_key.get(1).length() == 32*2
        resp.data.result.priv_key.get(1).length() == 64*2
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

    def "test create solidity contract and call event via rpc"() {
        given: 'a valid Solidity contract'
        String contract = '606060405234610000575b610251806100196000396000f300606060405263ffffffff60e060020a60003504166351973ec9811461003a57806394e8767d14610049578063b60e72cc1461006b575b610000565b34610000576100476100c2565b005b346100005761005960043561010e565b60408051918252519081900360200190f35b3461000057610047600480803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843750949650509335935061016b92505050565b005b6040805173ffffffffffffffffffffffffffffffffffffffff3016815290517fb123f68b8ba02b447d91a6629e121111b7dd6061ff418a60139c8bf00522a2849181900360200190a15b565b600081151561013e57507f3000000000000000000000000000000000000000000000000000000000000000610164565b5b600082111561016457600a808304920660300160f860020a026101009091041761013f565b5b5b919050565b7f614c60883eb4fb2805fb25f372e0dd0572fbbd69edd43a764af7384df6785275826101968361010e565b604080516020808201849052828252845192820192909252835190918291606083019186019080838382156101e6575b8051825260208311156101e657601f1990920191602091820191016101c6565b505050905090810190601f1680156102125780820380516001836020036101000a031916815260200191505b50935050505060405180910390a15b50505600a165627a7a7230582080e6735eb8468c8ccb409bea165ee0b32ce38d4a1e206791d24d2c42720a3f870029'
        def caller = [
                address:'71044204395934D638C3BDA59E89C8219330A574',
                pub_key:'CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20',
                priv_key:'4487A3ED876CE4BB95C5E4982E5EB64BA4FADE2E7F1125F80F910EB9BE78DB48CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20'
        ]
        def callee = [
                address:'33F71BB66F8994DD099C0E360007D4DEAE11BFFE',
                priv_key:'4487A3ED876CE4BB95C5E4982E5EB64BA4FADE2E7F1125F80F910EB9BE78DB48CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20'
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

        and: 'the callee address is remembered'
        callee.address = resp.data.result.call_data.callee

        then: 'a valid response is received'
        resp.data.result.exception == ''
        resp.data.result.return  != null
        resp.data.result.origin == caller.address
        resp.data.result.call_data.caller == caller.address
        resp.data.result.call_data.callee == callee.address
        resp.data.result.tx_id != null

        when: println '(2) the newly created contract account is verified'
        request = ['id': '2', 'jsonrpc': '2.0', 'method': 'burrow.getAccount', 'params': ['address':callee.address]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.address == callee.address
        resp.data.result.code != null
        resp.data.result.balance == 0
        resp.data.result.sequence == 0
        resp.data.result.storage_root == ''
        resp.data.result.permissions.base.perms == 2302
        resp.data.result.permissions.base.set == 16383

        when: println '(3) the storage of the contract is retrieved'
        request = ['id': '3', 'jsonrpc': '2.0', 'method': 'burrow.getStorageAt', 'params': ['address':callee.address]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.key == ''
        resp.data.result.value == ''

        when: println '(4) we subscribe to events from the the new contract account'
        request = ['id': '4', 'jsonrpc': '2.0', 'method': 'burrow.eventSubscribe', 'params': ['event_id':'Log/'+callee.address]]
        resp = send request

        and: 'the event id is remembered'
        def subId = resp.data.result.sub_id

        and: println '(5) we poll for events'
        request = ['id': '5', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':subId]]
        resp = send request

        then: 'no events exist'
        resp.data.result.events == []

        when: println '(6) the contract is executed 3 times'
        3.times {
            request = ['id': '6', 'jsonrpc': '2.0', 'method': 'burrow.call', 'params': ['address':callee.address, 'data':'51973ec9']]
            resp = send request
        }

        then: 'a valid response is received'
        resp.data.result.gas_used == 69
        resp.data.result.return == ''

        when: println '(7) we poll for events again'
        request = ['id': '7', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':subId]]
        resp = send request

        then: 'an event for each call is received'
        resp.data.result.events.size() == 3
        resp.data.result.events.get(0).data == '000000000000000000000000' + callee.address

        when: println '(8) we poll for events again'
        request = ['id': '8', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':subId]]
        resp = send request

        then: 'no events exist'
        resp.data.result.events.size() == 0
    }

    def "test create solidity contract and store/update data via rpc"() {
        given: 'a valid Solidity contract'
        String contract = '606060405234610000575b60056000555b5b610235806100206000396000f3006060604052361561005c5763ffffffff60e060020a6000350416632a1afcd9811461006157806351973ec91461008057806360fe47b11461008f5780636d4ce63c146100a157806394e8767d146100c0578063f82c50f1146100e2575b610000565b346100005761006e6100f4565b60408051918252519081900360200190f35b346100005761008d6100fa565b005b346100005761008d600435610146565b005b346100005761006e610158565b60408051918252519081900360200190f35b346100005761006e60043561016d565b60408051918252519081900360200190f35b346100005761008d6004356101ca565b005b60005481565b6040805173ffffffffffffffffffffffffffffffffffffffff3016815290517fb123f68b8ba02b447d91a6629e121111b7dd6061ff418a60139c8bf00522a2849181900360200190a15b565b6000819055610154816101ca565b5b50565b60006101656000546101ca565b506000545b90565b600081151561019d57507f30000000000000000000000000000000000000000000000000000000000000006101c3565b5b60008211156101c357600a808304920660300160f860020a026101009091041761019e565b5b5b919050565b7f0b8ddb692aa3c458d373cbf20864c9657255684df41a8311d7e9e02b47deeb266101f48261016d565b60408051918252519081900360200190a15b505600a165627a7a72305820ed08de290454ab9e0676e75a3644fc09273208b3a475a6d224c886b5b0c6cccf0029'
        def caller = [
                address:'71044204395934D638C3BDA59E89C8219330A574',
                pub_key:'CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20',
                priv_key:'4487A3ED876CE4BB95C5E4982E5EB64BA4FADE2E7F1125F80F910EB9BE78DB48CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20'
        ]
        def callee = [
                address:'33F71BB66F8994DD099C0E360007D4DEAE11BFFE',
                priv_key:'4487A3ED876CE4BB95C5E4982E5EB64BA4FADE2E7F1125F80F910EB9BE78DB48CEE962D85B97CA3334AC95399F9A0A8563375A98712EE79320018BCFFA3AAA20'
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

        and: 'the callee address is remembered'
        callee.address = resp.data.result.call_data.callee

        then: 'a valid response is received'
        resp.data.result.exception == ''
        resp.data.result.return != null
        resp.data.result.origin == caller.address
        resp.data.result.call_data.caller == caller.address
        resp.data.result.call_data.callee == callee.address
        resp.data.result.tx_id != null

        when: println '(2) the newly created contract account is verified'
        request = ['id': '2', 'jsonrpc': '2.0', 'method': 'burrow.getAccount', 'params': ['address':callee.address]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.address == callee.address
        resp.data.result.code != null
        resp.data.result.balance == 0
        resp.data.result.sequence == 0
        resp.data.result.storage_root == 'FF0ECB0713DB23320850E1348FB3A419732EBC84'
        resp.data.result.permissions.base.perms == 2302
        resp.data.result.permissions.base.set == 16383

        when: println '(3) the storage of the contract is retrieved'
        request = ['id': '3', 'jsonrpc': '2.0', 'method': 'burrow.getStorage', 'params': ['address':callee.address]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.storage_root == 'FF0ECB0713DB23320850E1348FB3A419732EBC84'
        resp.data.result.storage_items.get(0).key == '0000000000000000000000000000000000000000000000000000000000000000'
        resp.data.result.storage_items.get(0).value == '0000000000000000000000000000000000000000000000000000000000000005'

        when: println '(4) we subscribe to events from the the new contract account'
        request = ['id': '4', 'jsonrpc': '2.0', 'method': 'burrow.eventSubscribe', 'params': ['event_id':'Log/'+callee.address]]
        resp = send request

        and: 'the event id is remembered'
        def subId = resp.data.result.sub_id

        and: println '(5) we poll for events'
        request = ['id': '5', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':subId]]
        resp = send request

        then: 'no events exist'
        resp.data.result.events == []

        when: println '(6) the get contract method is executed'
        request = ['id': '6', 'jsonrpc': '2.0', 'method': 'burrow.call', 'params': ['address':callee.address, 'data':'6d4ce63c']]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.return == '0000000000000000000000000000000000000000000000000000000000000005'

        when: println '(7) the set contract method is executed'
        params = [
                'priv_key':callee.priv_key,
                'data':'60fe47b10000000000000000000000000000000000000000000000000000000000000007',
                'address':callee.address,
                'fee':12,
                'gas_limit':223,
        ]
        request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.transactAndHold', 'params': params]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.exception == ''
        resp.data.result.return != null
        resp.data.result.origin == caller.address
        resp.data.result.call_data.caller == caller.address
        resp.data.result.call_data.callee == callee.address
        resp.data.result.tx_id != null

        when: println '(8) the get contract method is executed again'
        request = ['id': '8', 'jsonrpc': '2.0', 'method': 'burrow.call', 'params': ['address':callee.address, 'data':'6d4ce63c']]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.return.size() == 64
        resp.data.result.return == '0000000000000000000000000000000000000000000000000000000000000007'

        when: println '(9) the storage of the contract is retrieved'
        request = ['id': '9', 'jsonrpc': '2.0', 'method': 'burrow.getStorage', 'params': ['address':callee.address]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.storage_root == 'CB39487A96B84E57F6BC407CE123E715E0FA1E2B'
        resp.data.result.storage_items.get(0).key == '0000000000000000000000000000000000000000000000000000000000000000'
        resp.data.result.storage_items.get(0).value == '0000000000000000000000000000000000000000000000000000000000000007'

        when: println '(10) we poll for events again'
        request = ['id': '10', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':subId]]
        resp = send request

        then: 'an event for each call is received'
        resp.data.result.events.size() == 3
        resp.data.result.events.get(1).data.size() == 64
        resp.data.result.events.get(1).data == '3700000000000000000000000000000000000000000000000000000000000000'
    }
}