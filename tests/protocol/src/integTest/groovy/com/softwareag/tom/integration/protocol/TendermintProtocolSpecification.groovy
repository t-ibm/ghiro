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
class TendermintProtocolSpecification extends RestClientBaseSpecification {

    def setup() {
        given: 'a REST client'
        client = new RESTClient("http://${config.node.host.ip}:${config.node.host.info.port}")
    }

    def "test 'status'"() {
        given: 'a valid HTTP request'
        def request = '/status'

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        resp.data.error == null
        resp.data.result.SyncInfo.LatestBlockHeight > 0
        resp.data.result.SyncInfo.LatestBlockHash != null
        resp.data.result.BurrowVersion.startsWith '0.29.1'
        resp.data.result.NodeInfo.Version == '0.32.4'
    }

    def "test 'validators'"() {
        given: 'a valid JSON-RPC request'
        def request = '/validators'

        when: 'the request is send'
        resp = send request

        then: 'a valid response is received'
        def validator = resp.data.result.BondedValidators.pop()
        validator.Address == '9505E4785FF66E23D8B1ECB47A1E49AA01D81C19'
        validator.PublicKey.PublicKey == '925527743DFA41BC98580F892155D3246321656A892E04BFAA7D11FA66A51350'
        validator.Power == 9999999999
    }
}