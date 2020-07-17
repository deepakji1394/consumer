package com.airtel.consumer.config;

import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Http client configuration
 *
 * @author dmalhotra
 * @version 1.0.0
 */
@Configuration
public class HttpClientConfiguration {

    /**
     * Determines the timeout in milliseconds until a connection is established.
     */
    private static final int CONNECT_TIMEOUT = 30000;

    /**
     * Timeout in milliseconds used when requesting a connection
     * from the connection manager.
     */
    private static final int REQUEST_TIMEOUT = 30000;

    /**
     * Maximum inactivity between two data packets
     */
    private static final int SOCKET_TIMEOUT = 30000;

    /**
     * Maximum number of connections for all the routes
     */
    private static final int MAX_CONN_TOTAL = 500;

    /**
     * Maximum number of connections for a particular route
     */
    private static final int MAX_CONN_PER_ROUTE = 300;

    /**
     * Get the http client bean
     *
     * @return the http client bean
     */
    @Bean
    public CloseableHttpClient httpClient() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        Header acceptHeader = new BasicHeader(HttpHeaders.ACCEPT, "application/json");

        List<Header> defaultHeaders = Lists.newArrayList(acceptHeader);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setMaxConnTotal(MAX_CONN_TOTAL)
                .setMaxConnPerRoute(MAX_CONN_PER_ROUTE)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultHeaders(defaultHeaders)
                .build();

        return httpClient;
    }
}
