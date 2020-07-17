package com.airtel.consumer.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents kafka server config properties.
 *
 * @version 1.0.0
 */
@Getter
@Setter
public class KafkaProperties {

    /**
     * list of topics to subscribe.
     */
    private String topics;

    /**
     * acks is used to get acknowledge for message sent to producer.
     */
    private String acks;

    /**
     * bootstrapServers is url of kafka broker servers.
     */
    private String bootstrapServers;

    /**
     * group in which kafka consumer resides.
     */
    private String group;

    /**
     * consumer key deserializer class.
     */
    private String keyDeserializer;

    /**
     * producer key serializer class.
     */
    private String keySerializer;

    /**
     * consumer value deserializer class.
     */
    private String valueDeserializer;

    /**
     * producer value serializer class.
     */
    private String valueSerializer;

    /**
     * max number of threads for message deliver.
     */
    private int numOfThreads;

    /**
     * max poll records.
     */
    private int maxPollRecords;

    /**
     * producer message retries.
     */
    private int retries;
    /**
     * Multipler to available processor
     */
    private int multipler;
}
