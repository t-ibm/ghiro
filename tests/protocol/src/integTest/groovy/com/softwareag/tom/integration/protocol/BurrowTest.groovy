/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.protocol

import com.softwareag.tom.contract.ConfigLocationFileSystem
import com.softwareag.tom.contract.Contract
import com.softwareag.tom.contract.abi.ContractInterface
import com.softwareag.tom.contract.ContractRegistry
import com.softwareag.tom.contract.SolidityLocationFileSystem
import com.softwareag.tom.util.HexValueBase
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

    def "test 'eventSubscribe' for new block via rpc"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.eventSubscribe', 'params': ['event_id':'NewBlock']]

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.sub_id.length() == 64
    }

    def "test create solidity contract and call event via rpc"() {
        given: 'a valid Solidity contract'
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/util/Console']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification logFunction = functions.get(0)
        assert logFunction.name == 'log'

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
                'data':contract.binary,
                'address':'',
                'fee':contract.gasPrice,
                'gas_limit':contract.gasLimit,
        ]
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.transactAndHold', 'params': params]

        when: println '(1) contract "sample/util/Console" gets deployed'
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

        when: println '(6) function "log" is executed 3 times'
        3.times {
            request = ['id': '6', 'jsonrpc': '2.0', 'method': 'burrow.call', 'params': ['address':callee.address, 'data':logFunction.encode([])]]
            resp = send request
        }

        then: 'a valid response is received'
        resp.data.result.gas_used == 84
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
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/SimpleStorage']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification setFunction = functions.get(1)
        assert setFunction.name == 'set'
        ContractInterface.Specification getFunction = functions.get(2)
        assert getFunction.name == 'get'
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
                'data':contract.binary,
                'address':'',
                'fee':contract.gasPrice,
                'gas_limit':contract.gasLimit,
        ]
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'burrow.transactAndHold', 'params': params]

        when: println '(1) contract "sample/SimpleStorage" gets deployed'
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
        def filterId = resp.data.result.sub_id

        and: println '(5) we poll for events'
        request = ['id': '5', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':filterId]]
        resp = send request

        then: 'no events exist'
        resp.data.result.events == []

        when: println '(6) function "get" is executed'
        request = ['id': '6', 'jsonrpc': '2.0', 'method': 'burrow.call', 'params': ['address':callee.address, 'data':getFunction.encode([])]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result.return == '0000000000000000000000000000000000000000000000000000000000000005'

        when: println '(7) function "set" is executed'
        params = [
                'priv_key':callee.priv_key,
                'data':setFunction.encode([BigInteger.valueOf(7)]),
                'address':callee.address,
                'fee':contract.gasPrice,
                'gas_limit':contract.gasLimit,
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

        when: println '(8) function "get" is executed again'
        request = ['id': '8', 'jsonrpc': '2.0', 'method': 'burrow.call', 'params': ['address':callee.address, 'data':getFunction.encode([])]]
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
        request = ['id': '10', 'jsonrpc': '2.0', 'method': 'burrow.eventPoll', 'params': ['sub_id':filterId]]
        resp = send request

        then: 'an event for each call is received'
        resp.data.result.events.size() == 1
        resp.data.result.events.get(0).data.size() == 64
        HexValueBase.decode(resp.data.result.events.get(0).data as String) == '7'

        when: println '(11) we unsubscribe to events from the the new contract account'
        request = ['id': '11', 'jsonrpc': '2.0', 'method': 'burrow.eventUnsubscribe', 'params': ['sub_id':filterId]]
        resp = send request

        then: 'the filter was successfully removed'
        resp.data.result
    }
}