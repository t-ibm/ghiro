/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.protocol

import groovyx.net.http.RESTClient

/**
 * System under specification: Tendermint endpoints.
 * @author tglaeser
 */
class TendermintTest extends RestClientSpecification {

    def setup() {
        given: 'a REST client'
        client = new RESTClient("http://${config.node.host.ip}:${config.node.host.tendermint.port}")
    }

    def "test 'status'"() {
        given: 'a valid HTTP request'
        def request = '/status'

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.error == ''
        def result = resp.data.result.pop()
        resp.data.result == [7]
        result.latest_block_hash != null
        result.latest_block_height > 0
    }
}