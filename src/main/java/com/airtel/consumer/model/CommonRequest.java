package com.airtel.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonRequest<T>{

    /**
     * The request method
     */
    @JsonProperty("method")
    private String method;

    /**
     * The api path
     */
    @JsonProperty("apiPath")
    private String apiPath;

    /**
     * The json payload
     */
    @JsonProperty("payload")
    private T payload;

}
