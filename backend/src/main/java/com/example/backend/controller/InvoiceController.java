package com.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice Categorization", description = "Endpoints for categorizing invoice items.")
public class InvoiceController {

    private final WebClient webClient;

    @Value("${python.api.base.url}")
    private String pythonApiBaseUrl;

    @Operation(summary = "Categorize an invoice item", description = "Receives an invoice item description, sends it to a Python ML service for categorization and returns the result.")
    @PostMapping("/api/categorize")
    public Mono<String> categorizeInvoiceItem(@RequestBody String invoiceItemDescription) {
        log.info("Received request to categorize invoice item: {}", invoiceItemDescription);

        Map<String, String> requestPayload = Map.of("description", invoiceItemDescription);

        return webClient.post()
                .uri(pythonApiBaseUrl + "/predict/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(Map.class)
                .map(responseMap -> (String) responseMap.get("category"))
                .doOnSuccess(category -> log.info("Successfully categorized invoice item. Category: {}", category))
                .doOnError(error -> log.error("Error categorizing invoice item: {}", error.getMessage()));
    }

    @Operation(summary = "Health check!", description = "Confirming the health of the system.")
    @GetMapping("/")
    public String healthCheck() {
        return "Invoice Identification Service is up and running!";
    }
}