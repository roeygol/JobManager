package com.jobmanager.orchestrator.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

/**
 * Service responsible for forwarding HTTP requests to target microservices.
 * Supports all HTTP methods and preserves headers, body, and query parameters.
 */
@Service
public class HttpForwardingService {

    private static final Logger logger = LoggerFactory.getLogger(HttpForwardingService.class);

    private final WebClient webClient;
    private final RemoteClientProperties properties;

    public HttpForwardingService(WebClient.Builder webClientBuilder, RemoteClientProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * Forwards an HTTP request to the target endpoint.
     *
     * @param method the HTTP method (GET, POST, PUT, DELETE, PATCH, etc.)
     * @param endpointUrl the full target endpoint URL
     * @param headers the request headers to forward
     * @param queryParams the query parameters to include
     * @param requestBody the request body (can be null)
     * @return HttpForwardingResponse containing the response and HTTP status
     */
    public HttpForwardingResponse forwardRequest(
            HttpMethod method,
            String endpointUrl,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Object requestBody) {
        
        logger.info("Forwarding {} request to: {}", method, endpointUrl);

        try {
            WebClient.RequestBodyUriSpec requestSpec = webClient.method(method);
            WebClient.RequestBodySpec uriSpec;

            // Build URI with query parameters (properly URL-encoded)
            URI uri;
            if (queryParams != null && !queryParams.isEmpty()) {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(endpointUrl);
                queryParams.forEach(uriBuilder::queryParam);
                uri = uriBuilder.build().toUri();
            } else {
                uri = URI.create(endpointUrl);
            }
            uriSpec = requestSpec.uri(uri);

            // Add headers (excluding Idempotency-Key as it's internal)
            // Handle Content-Type separately as it needs special handling
            String contentType = null;
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    String key = header.getKey();
                    if (!"Idempotency-Key".equalsIgnoreCase(key)) {
                        if ("Content-Type".equalsIgnoreCase(key)) {
                            contentType = header.getValue();
                        } else {
                            uriSpec.header(key, header.getValue());
                        }
                    }
                }
            }

            // Execute request and capture status code
            String responseBody = null;
            int httpStatus = 200;

            if (requestBody != null && (method == HttpMethod.POST || method == HttpMethod.PUT || 
                    method == HttpMethod.PATCH)) {
                // Set content type - use from headers if provided, otherwise default to JSON
                if (contentType != null) {
                    uriSpec.contentType(MediaType.parseMediaType(contentType));
                } else {
                    uriSpec.contentType(MediaType.APPLICATION_JSON);
                }
                
                // Use toEntity to capture status code
                ResponseEntity<String> responseEntity = uriSpec
                        .bodyValue(requestBody)
                        .retrieve()
                        .onStatus(status -> status.isError(), response -> {
                            logger.warn("Remote service returned error status: {}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(new WebClientResponseException(
                                            response.statusCode().value(),
                                            "Remote service error",
                                            response.headers().asHttpHeaders(),
                                            body.getBytes(),
                                            null
                                    )));
                        })
                        .toEntity(String.class)
                        .timeout(java.time.Duration.ofMillis(properties.getReadTimeout()))
                        .block();
                
                if (responseEntity != null) {
                    responseBody = responseEntity.getBody();
                    httpStatus = responseEntity.getStatusCode().value();
                }
            } else {
                // For GET, DELETE, etc. without body
                ResponseEntity<String> responseEntity = uriSpec
                        .retrieve()
                        .onStatus(status -> status.isError(), response -> {
                            logger.warn("Remote service returned error status: {}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(new WebClientResponseException(
                                            response.statusCode().value(),
                                            "Remote service error",
                                            response.headers().asHttpHeaders(),
                                            body.getBytes(),
                                            null
                                    )));
                        })
                        .toEntity(String.class)
                        .timeout(java.time.Duration.ofMillis(properties.getReadTimeout()))
                        .block();
                
                if (responseEntity != null) {
                    responseBody = responseEntity.getBody();
                    httpStatus = responseEntity.getStatusCode().value();
                }
            }

            logger.info("Request forwarded successfully. Response length: {}", 
                    responseBody != null ? responseBody.length() : 0);

            return new HttpForwardingResponse(responseBody, httpStatus);

        } catch (WebClientResponseException e) {
            logger.error("Remote service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return new HttpForwardingResponse(
                    e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getMessage(), 
                    e.getStatusCode().value());

        } catch (Exception e) {
            logger.error("Failed to forward request to {}: {}", endpointUrl, e.getMessage(), e);
            return new HttpForwardingResponse("Network error: " + e.getMessage(), 0);
        }
    }

    /**
     * Response wrapper for HTTP forwarding operations.
     */
    public static class HttpForwardingResponse {
        private final String responseBody;
        private final int httpStatus;

        public HttpForwardingResponse(String responseBody, int httpStatus) {
            this.responseBody = responseBody;
            this.httpStatus = httpStatus;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public int getHttpStatus() {
            return httpStatus;
        }

        public boolean isSuccess() {
            return httpStatus >= 200 && httpStatus < 300;
        }
    }
}

