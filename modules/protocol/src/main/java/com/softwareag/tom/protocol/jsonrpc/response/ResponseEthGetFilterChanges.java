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
import java.util.stream.Collectors;

/**
 * {@code eth_getFilterChanges}.
 */
public class ResponseEthGetFilterChanges extends Response<ResponseEthGetFilterChanges.Result, Types.ResponseEthGetFilterChanges> {

    public ResponseEthGetFilterChanges() {
        super();
    }

    public ResponseEthGetFilterChanges(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResponseEthGetFilterChanges(List<Event> events) {
        super();
        this.result = new Result(events);
    }

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
                            .addAllTopic(logEvent.topics.stream().map(Topic::getTopic).map(HexValue::toByteString).collect(Collectors.toList()))
                            .build()
                        )
                    ).build();
                }
            }
            return builder.build();
        }
    }

    final static class Result {
        @JsonDeserialize(using = LogResultDeserializer.class) @JsonProperty("events") public List<Event> events;

        private Result() {}

        private Result(List<Event> events) {
            this();
            this.events = events;
        }

        @Override public String toString() {
            return "{events:" + events + '}';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return events.equals(result.events);
        }

        @Override public int hashCode() {
            return events.hashCode();
        }
    }

    private static class LogResultDeserializer extends JsonDeserializer<List<Event>> {

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

    public final static class Log implements Event<Log> {
        @JsonProperty("address") public String address;
        @JsonProperty("data") public String data;
        @JsonProperty("height") public long height;
        @JsonProperty("topics") public List<Topic> topics;

        private Log() {}

        public Log(String address, String data, long height, List<String> topics) {
            this();
            this.address = address;
            this.data = data;
            this.height = height;
            this.topics = topics.stream().map(Topic::new).collect(Collectors.toList());
        }

        @Override public Log get() {
            return this;
        }

        @Override public String toString() {
            return "{\"address\":\"" + address + "\", \"data\":\"" + data + "\", \"height\":" + height + ", \"topics\":" + topics + '}';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Log log = (Log) o;

            if (height != log.height) return false;
            if (!address.equals(log.address)) return false;
            if (!data.equals(log.data)) return false;
            return topics.equals(log.topics);
        }

        @Override public int hashCode() {
            int result = address.hashCode();
            result = 31 * result + data.hashCode();
            result = 31 * result + (int) (height ^ (height >>> 32));
            result = 31 * result + topics.hashCode();
            return result;
        }
    }

    public final static class Topic {
        String topic;

        private Topic(String topic) {
            this.topic = topic;
        }

        public String getTopic() {
            return topic;
        }

        @Override public String toString() {
            return '\"' + topic + '\"';
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Topic topic1 = (Topic) o;

            return topic.equals(topic1.topic);
        }

        @Override public int hashCode() {
            return topic.hashCode();
        }
    }
}