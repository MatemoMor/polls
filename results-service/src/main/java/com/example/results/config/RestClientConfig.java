package com.example.results.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient pollRestClient(RestClient.Builder builder,
                                     @Value("${app.poll-service-url}") String baseUrl,
                                     @Value("${app.http.connect-timeout-ms}") int connectTimeoutMs,
                                     @Value("${app.http.read-timeout-ms}") int readTimeoutMs) {
        return build(builder, baseUrl, connectTimeoutMs, readTimeoutMs);
    }

    @Bean
    public RestClient voteRestClient(RestClient.Builder builder,
                                     @Value("${app.vote-service-url}") String baseUrl,
                                     @Value("${app.http.connect-timeout-ms}") int connectTimeoutMs,
                                     @Value("${app.http.read-timeout-ms}") int readTimeoutMs) {
        return build(builder, baseUrl, connectTimeoutMs, readTimeoutMs);
    }

    private static RestClient build(RestClient.Builder builder, String baseUrl, int connectMs, int readMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectMs);
        factory.setReadTimeout(readMs);
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }
}
