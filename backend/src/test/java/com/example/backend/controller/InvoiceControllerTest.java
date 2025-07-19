package com.example.backend.controller;

import com.example.backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private InvoiceController invoiceController;

    @BeforeEach
    void setUp() {
        // Manually inject the mocked WebClient into the controller
        // The @Value annotation for pythonApiBaseUrl is not resolved in unit tests
        // so we need to set it manually or mock the environment.
        // For simplicity, we'll mock the WebClient behavior directly.
        invoiceController = new InvoiceController(WebClient.builder(), "http://localhost:8000");
        // Re-inject mocks after manual instantiation
        // This part is tricky with @InjectMocks and manual constructor. 
        // A better approach for testing WebClient is to use @RestClientTest or mock the WebClient.Builder.
        // For this example, we'll mock the chain of calls.

        webTestClient = WebTestClient.bindToController(invoiceController)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void categorizeInvoiceItem_shouldReturnCategoryFromPythonApi() {
        String invoiceItemDescription = "Test Invoice Item";
        String expectedCategory = "Test Category";

        // Mock the WebClient chain
        when(webClient.post()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.bodyValue(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedCategory));

        // Re-initialize controller with mocked webClient
        invoiceController = new InvoiceController(WebClient.builder(), "http://localhost:8000");
        // This is a workaround. Proper way is to use @MockBean with SpringBootTest or configure WebClient mock properly.
        // For a simple unit test, we can directly set the webClient field if it were not final.
        // Since it's final, we need to mock the builder or use a different testing strategy.
        // Let's adjust the setup to properly mock the WebClient passed to the controller.
        // The @InjectMocks will handle the injection if the constructor is properly mocked.

        // Re-thinking the setup for a final WebClient field:
        // We need to mock the WebClient.Builder and then the build() method to return our mocked WebClient.
        // Or, make the WebClient field non-final for testing purposes (less ideal).
        // Let's adjust the @BeforeEach to properly mock the WebClient that gets injected.

        // Corrected @BeforeEach logic:
        WebClient.Builder webClientBuilder = WebClient.builder();
        WebClient mockWebClient = org.mockito.Mockito.mock(WebClient.class);
        when(webClientBuilder.baseUrl(any(String.class))).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        invoiceController = new InvoiceController(webClientBuilder, "http://localhost:8000");

        // Now mock the behavior of the `mockWebClient`
        when(mockWebClient.post()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.bodyValue(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedCategory));

        webTestClient = WebTestClient.bindToController(invoiceController)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();

        webTestClient.post().uri("/api/categorize")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(invoiceItemDescription)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(expectedCategory);
    }
}
