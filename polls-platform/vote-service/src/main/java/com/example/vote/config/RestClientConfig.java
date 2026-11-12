package com.example.vote.config;

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
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }
}
