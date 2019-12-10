/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc

import com.softwareag.tom.ObjectMapperFactory
import com.softwareag.tom.protocol.abi.Types
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthCall
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetBalance
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthGetFilterChanges
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthNewFilter
import com.softwareag.tom.protocol.jsonrpc.request.RequestEthUninstallFilter
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthCall
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetBalance
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthGetFilterChanges
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthNewFilter
import com.softwareag.tom.protocol.jsonrpc.response.ResponseEthUninstallFilter
import com.softwareag.tom.protocol.util.HexValue
import org.hyperledger.burrow.execution.Exec
import org.hyperledger.burrow.rpc.RpcEvents

/**
 * Mocks protocol layer communications.
 * @author tglaeser
 */
class ResponseMock {
    public String contractAddress
    long height
    String filterId
    String eventSignatureHash
    ResponseEthGetFilterChanges responseEthGetFilterChanges

    ResponseMock() {
        height = 30
        this.contractAddress = '33F71BB66F8994DD099C0E360007D4DEAE11BFFE'
        this.filterId = 'F3449755BA20C7BB6DB1B3433C2172096AA0033DABF7FD43388A3110BC8BEA5D'
        this.eventSignatureHash = 'b123f68b8ba02b447d91a6629e121111b7dd6061ff418a60139c8bf00522a284'// Keccak-256 hash for 'LogAddress(address)'
        String responseEthGetFilterChangesContent = '{"id":42, "jsonrpc":"2.0", "result":{events:[' +
            '{"address":"' + contractAddress + '", "data":"00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE", "height":' + height + ', "topics":["' + eventSignatureHash + '", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]},' +
            '{"address":"' + contractAddress + '", "data":"00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE", "height":' + height + ', "topics":["' + eventSignatureHash + '", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]},' +
            '{"address":"' + contractAddress + '", "data":"00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE", "height":' + height + ', "topics":["' + eventSignatureHash + '", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6861686100000000000000000000000000000000000000000000000000000000"]}]}}'
        this.responseEthGetFilterChanges = ObjectMapperFactory.getJsonMapper().readValue(responseEthGetFilterChangesContent, ResponseEthGetFilterChanges.class)
    }

    List<Types.FilterLogType> getExpectedFilterChangesWeb3() {
        List<Types.FilterLogType> expected = []
        for (ResponseEthGetFilterChanges.Event event : responseEthGetFilterChanges.events) {
            if (event instanceof ResponseEthGetFilterChanges.Log) {
                ResponseEthGetFilterChanges.Log logEvent = ((ResponseEthGetFilterChanges.Log) event).get()
                expected.add Types.FilterLogType.newBuilder()
                    .setAddress(HexValue.toByteString(logEvent.address))
                    .setData(HexValue.toByteString(logEvent.data))
                    .setBlockNumber(HexValue.toByteString(logEvent.height))
                    .addAllTopic(logEvent.topics.collect {t -> HexValue.toByteString(t.getTopic())})
                    .build()
            }
        }
        return expected
    }

    List<RpcEvents.EventsResponse> getExpectedFilterChangesBurrow() {
        List<RpcEvents.EventsResponse> expected = []
        expected.add RpcEvents.EventsResponse.newBuilder().setHeight(height).addEvents(
            Exec.Event.newBuilder().setHeader(
                Exec.Header.newBuilder().setTxType(2).setTxHash(HexValue.copyFrom('0xa8af028a6aa5a15ffbc6bd80795e0731f7f0b4f2777b4ea006ed97e878e1aaec')).setEventType(2).setEventID("Log/32B11B5AE572F59C0345223EC2403B7A91FD2DA2").setHeight(height).build()
            ).setLog(
                Exec.LogEvent.newBuilder().setAddress(HexValue.copyFrom('0x32B11B5AE572F59C0345223EC2403B7A91FD2DA2')).setData(HexValue.copyFrom('0x00000000000000000000000033F71BB66F8994DD099C0E360007D4DEAE11BFFE')).addTopics(HexValue.copyFrom('0xb123f68b8ba02b447d91a6629e121111b7dd6061ff418a60139c8bf00522a284')).build()
            ).build()
        ).build()
        return expected
    }

    Response getResponse(Request request) {
        String responseEthNewFilterContent = '{"id":42, "jsonrpc":"2.0", "result":{"sub_id":"' + filterId + '"}}'
        String responseEthUninstallFilterContent = '{"id":42, "jsonrpc":"2.0", "result":{"result":"true"}}'
        String responseEthGetBalanceContent = '{"id":42, "jsonrpc":"2.0", "result":"0x0234c8a3397aab58"}'
        String responseEthCallContent = '{"id":42, "jsonrpc":"2.0", "result":"0x"}'

        ResponseEthNewFilter responseEthNewFilter = ObjectMapperFactory.getJsonMapper().readValue(responseEthNewFilterContent, ResponseEthNewFilter.class)
        ResponseEthUninstallFilter responseEthUninstallFilter = ObjectMapperFactory.getJsonMapper().readValue(responseEthUninstallFilterContent, ResponseEthUninstallFilter.class)
        ResponseEthGetBalance responseEthGetBalance = ObjectMapperFactory.getJsonMapper().readValue(responseEthGetBalanceContent, ResponseEthGetBalance.class)
        ResponseEthCall responseEthCall = ObjectMapperFactory.getJsonMapper().readValue(responseEthCallContent, ResponseEthCall.class)

        Response response
        if (request instanceof RequestEthNewFilter) {
            response = responseEthNewFilter
        } else if (request instanceof RequestEthGetFilterChanges) {
            response = responseEthGetFilterChanges
        } else if (request instanceof RequestEthUninstallFilter) {
            response = responseEthUninstallFilter
        } else if (request instanceof RequestEthGetBalance) {
            response = responseEthGetBalance
        } else if (request instanceof RequestEthCall) {
            response = responseEthCall
        } else {
            response = null
        }
        response
    }
}