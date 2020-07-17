package com.airtel.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignOrderRequest {

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("delivery_man_id")
    private Long deliveryManId;
}
