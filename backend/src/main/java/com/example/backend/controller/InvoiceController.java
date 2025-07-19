package com.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final WebClient webClient;

    @Value("${python.api.base.url}")
    private String pythonApiBaseUrl;

    @PostMapping("/api/categorize")
    public Mono<String> categorizeInvoiceItem(@RequestBody String invoiceItemDescription) {
        log.info("Received request to categorize invoice item: {}", invoiceItemDescription);
        return webClient.post()
                .uri(pythonApiBaseUrl + "/predict")
                .bodyValue(invoiceItemDescription)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(category -> log.info("Successfully categorized invoice item. Category: {}", category))
                .doOnError(error -> log.error("Error categorizing invoice item: {}", error.getMessage()));
    }
}