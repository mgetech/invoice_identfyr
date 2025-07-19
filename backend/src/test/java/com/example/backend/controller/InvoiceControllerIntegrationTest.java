package com.example.backend.controller;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InvoiceControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient; // Use Spring's WebTestClient to test the actual endpoint

    private WireMockServer wireMockServer;

    // We'll override the base URL for the Python API to point to WireMock
    // This is typically done in application-test.properties or by using @TestPropertySource
    // For simplicity here, we'll manually set it up with WireMock's port.

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8000); // Or a random port if you prefer
        wireMockServer.start();
        // Point your Spring application's python.api.base.url to WireMock's URL
        // In a real project, you'd use @TestPropertySource or modify application.yml/properties
        // For this example, we're assuming the controller is configured dynamically or through WebClient bean.
        // If your controller uses @Value, you need to configure it via Spring Boot test properties.
        // Example: If @Value("${python.api.base.url}") in controller, set it in test config
        System.setProperty("python.api.base.url", "http://localhost:" + wireMockServer.port());

        // Initialize WireMock client
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
        // Clean up system property
        System.clearProperty("python.api.base.url");
    }

    @Test
    void categorizeInvoiceItem_shouldReturnCategoryFromPythonApi() {
        String invoiceItemDescription = "Test Invoice Item";
        String expectedCategory = "Test Category";

        // Stub WireMock to respond to the POST /predict request
        WireMock.stubFor(post(urlEqualTo("/predict"))
                .withRequestBody(WireMock.equalTo(invoiceItemDescription)) // Match the exact body
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .withStatus(200)
                        .withBody(expectedCategory)));

        // Make the actual HTTP call to your Spring Boot application's endpoint
        webTestClient.post().uri("/api/categorize")
                .contentType(MediaType.TEXT_PLAIN) // Ensure this matches your controller's expected content type
                .bodyValue(invoiceItemDescription)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(expectedCategory);

        // Verify that your application made the expected call to WireMock
        WireMock.verify(WireMock.postRequestedFor(urlEqualTo("/predict"))
                .withRequestBody(WireMock.equalTo(invoiceItemDescription)));
    }

    @Test
    void categorizeInvoiceItem_shouldHandlePythonApiError() {
        String invoiceItemDescription = "Error Item";
        String errorMessage = "Python API Internal Server Error";

        WireMock.stubFor(post(urlEqualTo("/predict"))
                .withRequestBody(WireMock.equalTo(invoiceItemDescription))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .withBody(errorMessage)));

        webTestClient.post().uri("/api/categorize")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(invoiceItemDescription)
                .exchange()
                .expectStatus().is5xxServerError() // Or whatever status your GlobalExceptionHandler maps it to
                .expectBody(String.class)
                .isEqualTo("Error categorizing invoice item: " + errorMessage); // Match your error response
    }
}