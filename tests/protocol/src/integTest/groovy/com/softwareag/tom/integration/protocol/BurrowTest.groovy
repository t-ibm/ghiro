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
        def request = ['jsonrpc': '2.0', 'id': '1', 'method': 'burrow.getClientVersion']

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.client_version == '0.8.0'
    }

    def "test 'getAccount' via rpc"() {
        given: 'a valid JSON-RPC request'
        def request = ['jsonrpc': '2.0', 'id': '1', 'method': 'burrow.getAccount', 'params': ['address':'E9B5D87313356465FAE33C406CE2C2979DE60BCB']]

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.result.address == 'E9B5D87313356465FAE33C406CE2C2979DE60BCB'
        resp.data.result.balance == 200000000
        resp.data.result.code == ''
        resp.data.result.pub_key == null
        resp.data.result.sequence == 0
        resp.data.result.storage_root == ''
    }
}