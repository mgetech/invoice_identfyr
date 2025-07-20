package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // This @Value will be overridden in the test
    @Value("${python.api.base.url}")
    private String pythonApiBaseUrl;

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        // Build the WebClient using the base URL from properties
        return webClientBuilder.baseUrl(pythonApiBaseUrl).build();
    }

    // It's also good practice to expose the builder itself if other clients need it
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}