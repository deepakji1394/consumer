package com.airtel.consumer.service;

import com.airtel.consumer.model.ActiveDeliverManResponse;
import com.airtel.consumer.util.ExternalCallUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ConsumerService implements CommandLineRunner {

    /**
     * Class level logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerService.class);

    /**
     * The jackson object mapper to parse any json records
     */
    @Autowired
    private ObjectMapper mapper;

    /**
     * The class level kafka consumer
     */
    @Autowired
    private KafkaConsumer<String, String> consumer;

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private ExternalCallUtil externalCallUtil;

    @Override
    public void run(String... args) throws Exception {
        runConsumer();
    }

    private void runConsumer() {
        {

            Duration duration = null;
            try {
                duration = Duration.ofSeconds(10);
                LOGGER.info("cosumer service running method runConsumer, duration:", duration);

                String topicStr = "orderTopic";

                // Subscribe to the topic.
                consumer.subscribe(Collections.unmodifiableList(Arrays.asList(topicStr.split(","))));
            } catch (Exception e) {
                LOGGER.error("Error occured while commit", e);
            }
            while (true) {
                long pollStart, processStart;
                pollStart = processStart = 0l;
                try {
                    pollStart = Instant.now().toEpochMilli();
                    LOGGER.info(String.format("start new poll: %d", pollStart));
                    final ConsumerRecords<String, String> consumerRecords = consumer.poll(duration);
                    LOGGER.info(String.format("Number of records: %s ,Poll duration: %d", consumerRecords.count(), Instant.now().toEpochMilli() - pollStart));
                    if (null == consumerRecords || consumerRecords.count() == 0) {
                        continue;
                    }
                    processStart = Instant.now().toEpochMilli();
                    process(consumerRecords);
                    LOGGER.info(String.format("Number of records: %s , Poll process duration from %d with duration: %d", consumerRecords.count(), processStart, Instant.now().toEpochMilli() - processStart));
                } catch (Exception e) {
                    LOGGER.error("error occured while collect/process", e);
                }
                try {
                    consumer.commitAsync();
                    LOGGER.info(String.format("commit last poll: %d", Instant.now().toEpochMilli()));
                } catch (Exception e) {
                    LOGGER.error("Error occured while commit", e);
                }
            }
        }
    }

    public void process(ConsumerRecords<String, String> records) throws Exception {
        long startTime;
        List<CompletableFuture<Boolean>> list = new ArrayList<>();
        startTime = Instant.now().toEpochMilli();

        List<ActiveDeliverManResponse> deliveryGuys = externalCallUtil.getAvailableDeliveryGuys();
        List<ActiveDeliverManResponse> availableDeliveryGuys = deliveryGuys.stream().filter(e->"AVAILABLE".equals(e.getStatus())).collect(Collectors.toList());

        int i=0;
        for (ConsumerRecord<String, String> record : records) {
            //completableFuture = null;
            if(i<availableDeliveryGuys.size()) {
                try {
                    LOGGER.info("_________________json request: " + record.value() + ", key: " + record.key() + ", partition: " + record.partition() + ", offset: " + record.offset() + ", topic: " + record.topic());
                    //submit sync with measuring elapsed time
                    list.add(placeOrderService.assignOrderToDeliveryMan(record, availableDeliveryGuys.get(i++)));
                } catch (Throwable e) {
                    LOGGER.error("Error while processing json message: " + record.value(), e);
                }
            } else {
                //can make sleep if avaliable isn't there for some time and check again
                deliveryGuys = externalCallUtil.getAvailableDeliveryGuys();
                availableDeliveryGuys = deliveryGuys.stream().filter(e->"AVAILABLE".equals(e.getStatus())).collect(Collectors.toList());
                i=0;
            }
        }

        //get all future objects
        if (!CollectionUtils.isEmpty(list))
            list.stream().forEach(future -> {
                try {
                    future.get();
                } catch (Exception e) {
                    LOGGER.error("Error: ", e);
                }
            });
        LOGGER.info(String.format("Time to execute last poll from %d with duration: %d", startTime, Instant.now().toEpochMilli() - startTime));
    }


}
