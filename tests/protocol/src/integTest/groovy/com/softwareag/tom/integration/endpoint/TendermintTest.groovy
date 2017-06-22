/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.endpoint

import com.softwareag.tom.extension.Node
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Shared
import spock.lang.Specification

class TendermintTest extends Specification {

    @Shared @Node protected ConfigObject config

    public "test 'status'"() {
        given: 'a REST client'
        RESTClient client = new RESTClient("http://${config.node.host.ip}:${config.node.host.tendermint.port}")

        when: 'we make a get request'
        HttpResponseDecorator resp = client.get(path: '/status', contentType: ContentType.JSON.toString()) as HttpResponseDecorator

        then: 'we receive a valid response'
        resp.success
        resp.status == 200
        resp.contentType == ContentType.JSON.toString()
        println "response payload - $resp.data"
        resp.data.error == ''
        def result = resp.data.result.pop()
        resp.data.result == [7]
        result.latest_block_hash != null
        result.latest_block_height > 0
    }
}