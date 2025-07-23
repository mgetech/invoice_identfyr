package com.example.backend.controller;

import com.example.backend.BackendApplication;
import com.example.backend.exception.GlobalExceptionHandler;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = BackendApplication.class)
class InvoiceControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        registry.add("python.api.base.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @AfterAll
    static void tearDownAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        WireMock.configureFor("localhost", wireMockServer.port());
        WireMock.reset();
    }

    @Test
    void categorizeInvoiceItem_shouldReturnCategoryFromPythonApi() {
        String invoiceItemDescription = "Test Invoice Item";
        String expectedCategory = "Test Category";

        String requestJsonToPython = "{\"description\": \"" + invoiceItemDescription + "\"}";
        String responseJsonFromPython = "{\"category\": \"" + expectedCategory + "\"}";

        stubFor(post(urlEqualTo("/predict/"))
                .withRequestBody(equalToJson(requestJsonToPython))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBody(responseJsonFromPython)));

        webTestClient.post().uri("/api/categorize")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(invoiceItemDescription)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(expectedCategory);


        // Verify that your application made the expected call to WireMock
        verify(postRequestedFor(urlEqualTo("/predict/"))
                .withRequestBody(equalToJson(requestJsonToPython)));
    }

    @Test
    void categorizeInvoiceItem_shouldHandlePythonApi500Error() {
        String invoiceItemDescription = "Error Item";
        String requestJsonToPython = "{\"description\": \"" + invoiceItemDescription + "\"}";
        String pythonApiErrorMessage = "Internal Server Error from Python API";

        stubFor(post(urlEqualTo("/predict/"))
                .withRequestBody(equalToJson(requestJsonToPython))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .withBody(pythonApiErrorMessage)));

        webTestClient.post().uri("/api/categorize")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(invoiceItemDescription)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").isEqualTo(pythonApiErrorMessage)
                .jsonPath("$.path").isEqualTo("/api/categorize");
    }
}