/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.protocol

import com.softwareag.tom.extension.Node
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: Burrow endpoints.
 * @author tglaeser
 */
class BurrowTest extends Specification {

    @Shared @Node protected ConfigObject config
    @Shared protected RESTClient client

    def setup() {
        given: 'a REST client'
        client = new RESTClient("http://${config.node.host.ip}:${config.node.host.port}")
    }

    public "test 'getClientVersion' via http"() {
        when: 'we make a get request'
        HttpResponseDecorator resp = client.get(path: '/network/client_version', contentType: ContentType.JSON.toString()) as HttpResponseDecorator

        then: 'we receive a valid response'
        resp.success
        resp.status == 200
        resp.contentType == ContentType.TEXT.toString()
        println "response payload - $resp.data"
        resp.data == ['client_version': '0.8.0']
    }

    public "test 'getClientVersion' via rpc"() {
        given: 'a valid JSON-RPC request'
        def requestParams = ['jsonrpc': '2.0', 'id': '1', 'method': 'burrow.getClientVersion']

        when: 'we post the request'
        HttpResponseDecorator resp = client.post(path: '/rpc', contentType: ContentType.JSON.toString(), body: requestParams) as HttpResponseDecorator

        then: 'we receive a valid response'
        resp.success
        resp.status == 200
        resp.contentType == ContentType.TEXT.toString()
        println "response payload - $resp.data"
        resp.data.result.client_version == '0.8.0'
    }

    public "test 'getAccount' via rpc"() {
        given: 'a valid JSON-RPC request'
        def requestParams = ['jsonrpc': '2.0', 'id': '1', 'method': 'burrow.getAccount', 'params': ['address':'E9B5D87313356465FAE33C406CE2C2979DE60BCB']]

        when: 'we post the request'
        HttpResponseDecorator resp = client.post(path: '/rpc', contentType: ContentType.JSON.toString(), body: requestParams) as HttpResponseDecorator

        then: 'we receive a valid response'
        resp.success
        resp.status == 200
        resp.contentType == ContentType.TEXT.toString()
        println "response payload - $resp.data"
        resp.data.result.address == 'E9B5D87313356465FAE33C406CE2C2979DE60BCB'
        resp.data.result.balance == 200000000
        resp.data.result.code == ''
        resp.data.result.pub_key == null
        resp.data.result.sequence == 0
        resp.data.result.storage_root == ''
    }
}