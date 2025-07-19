package com.example.backend.controller;

import com.example.backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private WebClient mockWebClient; // This mock is still needed and used by the controller

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec; // For post().uri()
    @Mock
    private WebClient.RequestBodySpec requestBodySpec; // For bodyValue()
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec; // For retrieve()
    @Mock
    private WebClient.ResponseSpec responseSpec; // For bodyToMono()

    private InvoiceController invoiceController;

    @BeforeEach
    void setUp() {
        invoiceController = new InvoiceController(mockWebClient);

        // Initialize WebTestClient
        webTestClient = WebTestClient.bindToController(invoiceController)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void categorizeInvoiceItem_shouldReturnCategoryFromPythonApi() {
        String invoiceItemDescription = "Test Invoice Item";
        String expectedCategory = "Test Category";

        // Mock the WebClient chain of calls using the mockWebClient
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedCategory));

        webTestClient.post().uri("/api/categorize")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(invoiceItemDescription)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(expectedCategory);
    }
}