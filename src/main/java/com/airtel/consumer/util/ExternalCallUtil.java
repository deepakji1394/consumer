package com.airtel.consumer.util;

import com.airtel.consumer.handler.model.ApiException;
import com.airtel.consumer.handler.model.BadRequestException;
import com.airtel.consumer.handler.model.ErrorCode;
import com.airtel.consumer.handler.model.ErrorResponse;
import com.airtel.consumer.model.ActiveDeliverManResponse;
import com.airtel.consumer.model.AssignOrderRequest;
import com.airtel.consumer.model.AssignOrderResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class ExternalCallUtil {

    /**
     * private static class level logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalCallUtil.class);

    private static final String ACTIVE_DELIVERY_GUY = "/api/v1/allActiveDeliveryMen";

    private static final String ASSIGN_ORDER = "/api/v1/assignOrder";

    private static final String BASE_URL = "http://localhost:9080/deliveryService";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CloseableHttpClient httpClient;

    private List<ActiveDeliverManResponse> doCallExternalApiForList(HttpUriRequest httpUriRequest) throws ApiException {
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpUriRequest)) {
            String json = EntityUtils.toString(httpResponse.getEntity());
            EntityUtils.consume(httpResponse.getEntity());
            if (httpResponse.getStatusLine().getStatusCode() < 299 && httpResponse.getStatusLine().getStatusCode() >= 200) {
                // parse the response only if response can be parsed
                if (StringUtils.isNotBlank(json)) {
                    List<ActiveDeliverManResponse> recruiters = objectMapper.readValue(json, new TypeReference<List<ActiveDeliverManResponse>>() {
                    });
                    return recruiters;
                }
                return null;
            }

            // if any other status throw exception
            try {
                // parse the error response
                ErrorResponse errorResponse = objectMapper.readValue(json, ErrorResponse.class);

                // if the response code is 4xx throw bad request exception
                if (httpResponse.getStatusLine().getStatusCode() < 500 && httpResponse.getStatusLine().getStatusCode() >= 400) {
                    throw new BadRequestException(errorResponse.getErrorCode(), errorResponse.getErrorMessage());
                }

                throw new ApiException(errorResponse.getErrorCode(), json);
            } catch (IOException ex) {
                LOGGER.error("unexpected error while parsing response", ex);
                throw new ApiException(ErrorCode.EXTERNAL_SERVICE_INVALID_ERROR_RESPONSE, json);
            } catch (BadRequestException bre) {
                LOGGER.warn("unexpected bad request error", bre);
                throw bre;
            } catch (Exception ignore) {
                LOGGER.error("unexpected error while calling api", ignore);
                throw new ApiException(ErrorCode.EXTERNAL_SERVICE_INVALID_ERROR_CODE, json);
            }
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.EXTERNAL_SERVICE_ERROR, "failed to call external service", ex);
        }
    }

    public List<ActiveDeliverManResponse> getAvailableDeliveryGuys() throws ApiException {

        try {
            final URI baseUri = new URIBuilder(BASE_URL).build();

            final URIBuilder applicationUriBuilder = new URIBuilder(baseUri);

            if (StringUtils.isNotBlank(baseUri.getPath())) {
                applicationUriBuilder.setPath(String.format("%s%s", baseUri.getPath(), ACTIVE_DELIVERY_GUY));
            } else {
                applicationUriBuilder.setPath(ACTIVE_DELIVERY_GUY);
            }

            URI applicationURI = applicationUriBuilder.build();
            HttpPost httpPost = new HttpPost(applicationURI);
            return doCallExternalApiForList(httpPost);

        } catch (URISyntaxException | ApiException e) {
            throw new ApiException(ErrorCode.SERVER_ERROR, String.format(" api non 2xx status code, api error [%s]",e.getMessage()));
        }
    }

    public AssignOrderResponse assignOrderCall(Long orderId, Long deliveryManId) throws ApiException{
        try {
            final URI baseUri = new URIBuilder(BASE_URL).build();

            final URIBuilder applicationUriBuilder = new URIBuilder(baseUri);

            if (StringUtils.isNotBlank(baseUri.getPath())) {
                applicationUriBuilder.setPath(String.format("%s%s", baseUri.getPath(), ASSIGN_ORDER));
            } else {
                applicationUriBuilder.setPath(ASSIGN_ORDER);
            }

            URI applicationURI = applicationUriBuilder.build();
            HttpPost httpPost = new HttpPost(applicationURI);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(new AssignOrderRequest(orderId,deliveryManId)), ContentType.APPLICATION_JSON));
            return doCallExternalApi(httpPost, AssignOrderResponse.class);

        } catch (URISyntaxException | IOException e) {
            throw new ApiException(ErrorCode.SERVER_ERROR, String.format("send email api non 2xx status code, api error [%s]",e.getMessage()));
        }
    }

    private <T> T doCallExternalApi(HttpUriRequest httpUriRequest, Class<T> clazz) throws ApiException {
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpUriRequest)) {
            String json = EntityUtils.toString(httpResponse.getEntity());
            EntityUtils.consume(httpResponse.getEntity());
            if (httpResponse.getStatusLine().getStatusCode() < 299 && httpResponse.getStatusLine().getStatusCode() >= 200) {
                // parse the response only if response can be parsed
                if (StringUtils.isNotBlank(json)) {
                    T apiResponse = objectMapper.readValue(json, clazz);

                    return apiResponse;
                }
                return null;
            }

            // if any other status throw exception
            try {
                // parse the error response
                ErrorResponse errorResponse = objectMapper.readValue(json, ErrorResponse.class);

                // if the response code is 4xx throw bad request exception
                if (httpResponse.getStatusLine().getStatusCode() < 500 && httpResponse.getStatusLine().getStatusCode() >= 400) {
                    throw new BadRequestException(errorResponse.getErrorCode(), errorResponse.getErrorMessage());
                }

                throw new ApiException(errorResponse.getErrorCode(), json);
            } catch (IOException ex) {
                LOGGER.error("unexpected error while parsing response", ex);
                throw new ApiException(ErrorCode.EXTERNAL_SERVICE_INVALID_ERROR_RESPONSE, json);
            } catch (BadRequestException bre) {
                LOGGER.warn("unexpected bad request error", bre);
                throw bre;
            } catch (Exception ignore) {
                LOGGER.error("unexpected error while calling api", ignore);
                throw new ApiException(ErrorCode.EXTERNAL_SERVICE_INVALID_ERROR_CODE, json);
            }
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.EXTERNAL_SERVICE_ERROR, "failed to call external service", ex);
        }
    }
}
