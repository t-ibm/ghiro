/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.protocol

import com.softwareag.tom.contract.ConfigLocationFileSystem
import com.softwareag.tom.contract.Contract
import com.softwareag.tom.contract.ContractRegistry
import com.softwareag.tom.contract.SolidityLocationFileSystem
import com.softwareag.tom.contract.abi.ContractInterface
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

    def "test 'eth_getBalance'"() {
        given: 'a valid JSON-RPC request'
        def request = ['id': '1', 'jsonrpc': '2.0', 'method': 'eth_getBalance', 'params': ['0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19','latest']]

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        HexValueBase.toBigInteger(resp.data.result as String) <= 99999999999999 * Math.pow(10, 18) // 1 ETH = 10^18 Wei
        HexValueBase.toBigInteger(resp.data.result as String) >= 9999999999999 * Math.pow(10, 18) // 1 ETH = 10^18 Wei
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

    def "test create solidity contract and call event via rpc"() {
        given: 'a valid Solidity contract'
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/util/Console']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification functionLog = functions.get(0)
        assert functionLog.name == 'log'
        List events = contract.abi.events as List<ContractInterface.Specification>
        ContractInterface.Specification eventLogAddress = events.get(0)
        assert eventLogAddress.name == 'LogAddress'

        and: 'a test fixture'
        String caller = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        String txHash
        String contractAddress
        Map request

        when: println '(1) contract "sample/util/Console" gets deployed'
        def params = [
            'from':caller,
            'to':'',
            'gas':HexValueBase.toString(contract.gasLimit),
            'gasPrice':HexValueBase.toString(contract.gasPrice),
            'data':contract.binary,
        ]
        request = ['id': '1', 'jsonrpc': '2.0', 'method': 'eth_sendTransaction', 'params': params]
        resp = send request

        and: 'the transaction hash is remembered'
        txHash = resp.data.result

        then: 'a valid response is received'
        txHash.size() == 32 * 2 + 2

        when: println '(2) the transaction receipt gets requested'
        request = ['id': '2', 'jsonrpc': '2.0', 'method': 'eth_getTransactionReceipt', 'params': [txHash]]
        resp = send request

        and: 'the contract address is remembered'
        contractAddress = resp.data.result.contractAddress

        then: 'a valid response is received'
        contractAddress.size() == 20 * 2 + 2
        resp.data.result.exception == null
        resp.data.result.transactionIndex == '0x0'
        resp.data.result.transactionHash == txHash
        resp.data.result.status == '0x1'
        resp.data.result.from == caller
        resp.data.result.to == "null"

        when: println '(3) the newly created contract account is verified'
        request = ['id': '3', 'jsonrpc': '2.0', 'method': 'eth_getBalance', 'params': [contractAddress, "latest"]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result == '0x'

        when: println '(4) the storage of the contract is retrieved'
        request = ['id': '4', 'jsonrpc': '2.0', 'method': 'eth_getStorageAt', 'params': [contractAddress, "0x0", "latest"]]
        resp = send request

        then: 'a valid response is received' //TODO :: Not implemented yet by Burrow
        resp.data.error.code == -32603
        resp.data.error.message == 'Error 3: not found'

        when: println '(5) function "log" is executed 3 times'
        params = [
            'from':caller,
            'to':contractAddress,
            'gas':HexValueBase.toString(contract.gasLimit),
            'gasPrice':HexValueBase.toString(contract.gasPrice),
            'data':functionLog.encode(),
        ]
        3.times {
            request = ['id': '5', 'jsonrpc': '2.0', 'method': 'eth_call', 'params': params]
            resp = send request
        }

        then: 'a valid response is received'
        resp.data.result == '0x'
    }

    def "test create solidity contract and store/update data via rpc"() {
        given: 'a valid Solidity contract'
        Map  contracts = ContractRegistry.build(new SolidityLocationFileSystem(config.node.contract.registry.location as URI), new ConfigLocationFileSystem(config.node.config.location as URI)).load()
        Contract contract = contracts['sample/SimpleStorage']
        List functions = contract.abi.functions as List<ContractInterface.Specification>
        ContractInterface.Specification functionSet = functions.get(1)
        assert functionSet.name == 'set'
        ContractInterface.Specification functionGet = functions.get(2)
        assert functionGet.name == 'get'
        List events = contract.abi.events as List<ContractInterface.Specification>
        ContractInterface.Specification eventLogUint = events.get(1)
        assert eventLogUint.name == 'LogUint'

        and: 'a test fixture'
        String caller = '0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19'
        String txHash
        String contractAddress
        Map request

        when: println '(1) contract "sample/util/Console" gets deployed'
        def params = [
            'from':caller,
            'to':'',
            'gas':HexValueBase.toString(contract.gasLimit),
            'gasPrice':HexValueBase.toString(contract.gasPrice),
            'data':contract.binary,
        ]
        request = ['id': '1', 'jsonrpc': '2.0', 'method': 'eth_sendTransaction', 'params': params]
        resp = send request

        and: 'the transaction hash is remembered'
        txHash = resp.data.result

        then: 'a valid response is received'
        txHash.size() == 32 * 2 + 2

        when: println '(2) the transaction receipt gets requested'
        request = ['id': '2', 'jsonrpc': '2.0', 'method': 'eth_getTransactionReceipt', 'params': [txHash]]
        resp = send request

        and: 'the contract address is remembered'
        contractAddress = resp.data.result.contractAddress

        then: 'a valid response is received'
        contractAddress.size() == 20 * 2 + 2
        resp.data.result.exception == null
        resp.data.result.transactionIndex == '0x0'
        resp.data.result.transactionHash == txHash
        resp.data.result.status == '0x1'
        resp.data.result.from == caller
        resp.data.result.to == "null"

        when: println '(3) the newly created contract account is verified'
        request = ['id': '3', 'jsonrpc': '2.0', 'method': 'eth_getBalance', 'params': [contractAddress, "latest"]]
        resp = send request

        then: 'a valid response is received'
        resp.data.result == '0x'

        when: println '(4) the storage of the contract is retrieved'
        request = ['id': '4', 'jsonrpc': '2.0', 'method': 'eth_getStorageAt', 'params': [contractAddress, "0x0", "latest"]]
        resp = send request

        then: 'a valid response is received' //TODO :: Not implemented yet by Burrow
        resp.data.error.code == -32603
        resp.data.error.message == 'Error 3: not found'

        when: println '(5) function "get" is executed'
        params = [
            'from':caller,
            'to':contractAddress,
            'gas':HexValueBase.toString(contract.gasLimit),
            'gasPrice':HexValueBase.toString(contract.gasPrice),
            'data':functionGet.encode(),
        ]
        request = ['id': '5', 'jsonrpc': '2.0', 'method': 'eth_call', 'params': params]
        resp = send request

        then: 'a valid response is received'
        resp.data.result == '0x0000000000000000000000000000000000000000000000000000000000000005'

        when: println '(6) function "set" is executed'
        params = [
            'from':caller,
            'to':contractAddress,
            'gas':HexValueBase.toString(contract.gasLimit),
            'gasPrice':HexValueBase.toString(contract.gasPrice),
            'data':functionSet.encode([BigInteger.valueOf(7)]),
        ]
        request = ['id': '6', 'jsonrpc': '2.0', 'method': 'eth_sendTransaction', 'params': params]
        resp = send request

        and: 'the transaction hash is remembered'
        txHash = resp.data.result

        then: 'a valid response is received'
        txHash.size() == 32 * 2 + 2

        when: println '(7) function "get" is executed again'
        params = [
            'from':caller,
            'to':contractAddress,
            'gas':HexValueBase.toString(contract.gasLimit),
            'gasPrice':HexValueBase.toString(contract.gasPrice),
            'data':functionGet.encode(),
        ]
        request = ['id': '7', 'jsonrpc': '2.0', 'method': 'eth_call', 'params': params]
        resp = send request

        then: 'a valid response is received'
        resp.data.result == '0x0000000000000000000000000000000000000000000000000000000000000007'

        when: println '(8) the storage of the contract is retrieved'
        request = ['id': '8', 'jsonrpc': '2.0', 'method': 'eth_getStorageAt', 'params': [contractAddress, "0x0", "latest"]]
        resp = send request

        then: 'a valid response is received' //TODO :: Not implemented yet by Burrow
        resp.data.error.code == -32603
        resp.data.error.message == 'Error 3: not found'
    }
}