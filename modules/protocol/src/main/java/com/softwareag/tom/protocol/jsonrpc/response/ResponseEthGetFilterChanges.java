/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.softwareag.tom.ObjectMapperFactory;
import com.softwareag.tom.protocol.abi.Types;
import com.softwareag.tom.protocol.jsonrpc.Response;
import com.softwareag.tom.protocol.util.HexValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@code eth_getFilterChanges}.
 */
public class ResponseEthGetFilterChanges extends Response<ResponseEthGetFilterChanges.Result, Types.ResponseEthGetFilterChanges> {

    public Types.ResponseEthGetFilterChanges getResponse() {
        if (this.error != null) {
            throw new UnsupportedOperationException(this.error.message);
        } else {
            Types.ResponseEthGetFilterChanges.Builder builder = Types.ResponseEthGetFilterChanges.newBuilder();
            for (int i = 0; i < this.result.events.size(); i++) {
                Event event = this.result.events.get(i);
                if (event instanceof Log) {
                    Log logEvent = ((Log)event);
                    builder.addEvent(i, Types.FilterEventType.newBuilder().setLog(
                        Types.FilterLogType.newBuilder()
                            .setAddress(HexValue.toByteString(logEvent.address))
                            .setData(HexValue.toByteString(logEvent.data))
                            .setBlockNumber(HexValue.toByteString(logEvent.height))
                            .addAllTopic(logEvent.topics.stream().map(HexValue::toByteString).collect(Collectors.toList()))
                            .build()
                        )
                    ).build();
                }
            }
            return builder.build();
        }
    }

    static class Result {
        @JsonDeserialize(using = LogResultDeserialiser.class) @JsonProperty("events") public List<Event> events;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result result = (Result) o;
            return Objects.equals(events, result.events);
        }

        @Override public int hashCode() {
            return Objects.hash(events);
        }
    }

    public static class LogResultDeserialiser extends JsonDeserializer<List<Event>> {

        private ObjectReader objectReader = ObjectMapperFactory.getJsonMapper().reader();

        @Override public List<Event> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

            List<Event> logResults = new ArrayList<>();
            JsonToken nextToken = jsonParser.nextToken();

            if (nextToken == JsonToken.START_OBJECT) {
                Iterator<Log> logObjectIterator = objectReader.readValues(jsonParser, Log.class);
                while (logObjectIterator.hasNext()) {
                    logResults.add(logObjectIterator.next());
                }
            }
            return logResults;
        }
    }

    public List<Event> getEvents() {
        return this.result.events;
    }

    public interface Event<T> {
        T get();
    }

    public static class Log implements Event<Log> {
        @JsonProperty("address") public String address;
        @JsonProperty("data") public String data;
        @JsonProperty("height") public long height;
        @JsonProperty("topics") public List<String> topics;

        @Override public Log get() {
            return this;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Log log = (Log) o;
            return height == log.height &&
                    Objects.equals(address, log.address) &&
                    Objects.equals(data, log.data) &&
                    Objects.equals(topics, log.topics);
        }

        @Override public int hashCode() { return Objects.hash(address, data, height, topics); }
    }
}