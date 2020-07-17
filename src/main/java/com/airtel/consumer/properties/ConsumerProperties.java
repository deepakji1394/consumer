package com.airtel.consumer.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


/**
 * The type Cuckoo consumer.
 *
 * @author Prashant
 */
@Component
@ConfigurationProperties("consumer")
@Validated
@Getter@Setter
public class ConsumerProperties {
    /**
     * The app name
     */
    @NotEmpty
    private String appName;
    /**
     * The app version
     */
    @NotEmpty
    private String appVersion;
    /**
     * The user for actuator endpoints
     */

    @NotNull
    private KafkaProperties kafkaProperties;

    /**
     * Kafka topics comma seprated.
     */
    private int defaultPoolSize;

    /**
     * maxPoolSize is used to get acknowledge for message sent to producer.
     */
    private int maxPoolSize;

    /**
     * queueSize is url of kafka broker servers.
     */
    private int queueSize;
}
