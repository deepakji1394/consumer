package com.airtel.consumer.service;

import com.airtel.consumer.model.ActiveDeliverManResponse;
import com.airtel.consumer.model.AssignOrderResponse;
import com.airtel.consumer.model.CommonRequest;
import com.airtel.consumer.model.PlacedOrderSync;
import com.airtel.consumer.util.ExternalCallUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PlaceOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaceOrderService.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ExternalCallUtil externalCallUtil;

    @Async
    public CompletableFuture<Boolean> assignOrderToDeliveryMan(ConsumerRecord<String, String> record, ActiveDeliverManResponse activeDeliverManResponse) throws Exception {

        CommonRequest<PlacedOrderSync> commonRequest = null;
        try {
            commonRequest = mapper.readValue(record.value(), new TypeReference<CommonRequest<PlacedOrderSync>>() {
            });
        } catch (Exception e) {
            LOGGER.error("Error while parsing/saving", e);
        }

        if (commonRequest.getPayload() != null) {

            AssignOrderResponse response = externalCallUtil.assignOrderCall(commonRequest.getPayload().getOrderId(), activeDeliverManResponse.getId());
            if (response != null && "ACCEPTED".equals(response.getStatus()))
                return CompletableFuture.completedFuture(true);
        }
        return CompletableFuture.completedFuture(false);
    }
}
