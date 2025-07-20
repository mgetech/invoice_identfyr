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
        wireMockServer = new WireMockServer(0); // 0 means random available port
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
        WireMock.reset(); // Reset stubs before each test for isolation
    }

    @Test
    void categorizeInvoiceItem_shouldReturnCategoryFromPythonApi() {
        String invoiceItemDescription = "Test Invoice Item";
        String expectedCategory = "Test Category";

        // Define the JSON payload your Java controller will send
        String requestJsonToPython = "{\"description\": \"" + invoiceItemDescription + "\"}";
        // Define the JSON payload your Python API (mocked by WireMock) will return
        String responseJsonFromPython = "{\"category\": \"" + expectedCategory + "\"}";

        // Stub WireMock to respond to the POST /predict request
        stubFor(post(urlEqualTo("/predict/"))
                .withRequestBody(equalToJson(requestJsonToPython)) // WireMock expects this JSON body
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE) // WireMock should return JSON
                        .withStatus(200)
                        .withBody(responseJsonFromPython))); // WireMock response format

        // Make the actual HTTP call to your Spring Boot application's endpoint
        webTestClient.post().uri("/api/categorize")
                .contentType(MediaType.TEXT_PLAIN) // <--- Your Spring endpoint still accepts TEXT_PLAIN for its @RequestBody
                .bodyValue(invoiceItemDescription)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(expectedCategory); // Expecting the String category back


        // Verify that your application made the expected call to WireMock
        verify(postRequestedFor(urlEqualTo("/predict/"))
                .withRequestBody(equalToJson(requestJsonToPython))); // Verify it sent this JSON
    }

    @Test
    void categorizeInvoiceItem_shouldHandlePythonApi500Error() {
        String invoiceItemDescription = "Error Item";
        String requestJsonToPython = "{\"description\": \"" + invoiceItemDescription + "\"}";
        String pythonApiErrorMessage = "Internal Server Error from Python API"; // Message coming FROM Python API

        stubFor(post(urlEqualTo("/predict/"))
                .withRequestBody(equalToJson(requestJsonToPython))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE) // WireMock returns plain text error
                        .withBody(pythonApiErrorMessage))); // Python API's error message

        webTestClient.post().uri("/api/categorize")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(invoiceItemDescription)
                .exchange()
                .expectStatus().is5xxServerError() // Assert that it's a 5xx status code
                .expectHeader().contentType(MediaType.APPLICATION_JSON) // Assert that your handler returns JSON
                .expectBody()
                .jsonPath("$.status").isEqualTo(500) // Assert the 'status' field in the JSON
                .jsonPath("$.error").isEqualTo("Internal Server Error") // Assert the 'error' field
                .jsonPath("$.message").isEqualTo(pythonApiErrorMessage) // Assert the 'message' field matches the Python API's error
                .jsonPath("$.path").isEqualTo("/api/categorize"); // Assert the 'path' field
    }
}