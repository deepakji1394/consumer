package com.airtel.consumer.config;

import com.airtel.consumer.properties.ConsumerProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ConsumerConfiguration {

    @Autowired
    private ConsumerProperties properties;

    /**
     * Creates bean of kafka consumer {@link KafkaConsumer} object.
     *
     * @return KafkaConsumer
     */
    @Bean
    public KafkaConsumer getConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafkaProperties().getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, properties.getKafkaProperties().getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                properties.getKafkaProperties().getValueDeserializer());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getKafkaProperties().getGroup());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getKafkaProperties().getMaxPollRecords());
        return new KafkaConsumer<>(props);
    }

    /**
     * Async thread pool
     *
     * @return {@link AsyncTaskExecutor} The async thread executor
     */
    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getDefaultPoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setThreadNamePrefix(properties.getAppName() );
        executor.setQueueCapacity(properties.getQueueSize());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
