package com.jobmanager.orchestrator.application.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Isolated REST client abstraction for remote job execution.
 * Handles HTTP-level concerns and network communication.
 */
@Component
public class RemoteJobClient {

    private static final Logger logger = LoggerFactory.getLogger(RemoteJobClient.class);

    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private RemoteClientProperties properties;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * Executes a remote job call.
     *
     * @param endpointUrl the full endpoint URL
     * @param requestBody the request body (can be null)
     * @return RemoteJobResponse containing the response and HTTP status
     */
    public RemoteJobResponse executeJob(String endpointUrl, Object requestBody) {
        logger.info("Executing remote job call to: {}", endpointUrl);

        try {
            WebClient.RequestBodySpec requestSpec = webClient
                    .post()
                    .uri(endpointUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);

            if (requestBody != null) {
                requestSpec.bodyValue(requestBody);
            }

            String responseBody = requestSpec
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
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
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(properties.getRetryMaxAttempts(), 
                            Duration.ofMillis(properties.getRetryBackoffDelay()))
                            .filter(throwable -> {
                                // Only retry on 5xx server errors or network issues
                                if (throwable instanceof WebClientResponseException) {
                                    WebClientResponseException ex = (WebClientResponseException) throwable;
                                    return ex.getStatusCode() != null && ex.getStatusCode().is5xxServerError();
                                }
                                // Retry on network/timeout exceptions
                                return throwable instanceof java.util.concurrent.TimeoutException 
                                        || throwable instanceof java.net.ConnectException;
                            })
                            .doBeforeRetry(retrySignal -> 
                                    logger.info("Retrying remote call (attempt {}/{}): {}", 
                                            retrySignal.totalRetries() + 1, 
                                            properties.getRetryMaxAttempts(),
                                            retrySignal.failure().getMessage())))
                    .timeout(Duration.ofMillis(properties.getReadTimeout()))
                    .block();

            logger.info("Remote job call completed successfully. Response length: {}", 
                    responseBody != null ? responseBody.length() : 0);

            return new RemoteJobResponse(responseBody, 200);

        } catch (WebClientResponseException e) {
            logger.error("Remote service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return new RemoteJobResponse(e.getResponseBodyAsString(), e.getStatusCode().value());

        } catch (Exception e) {
            logger.error("Failed to execute remote job call to {}: {}", endpointUrl, e.getMessage(), e);
            return new RemoteJobResponse("Network error: " + e.getMessage(), 0);
        }
    }

    /**
     * Response wrapper for remote job execution.
     */
    public static class RemoteJobResponse {
        private final String responseBody;
        private final int httpStatus;

        public RemoteJobResponse(String responseBody, int httpStatus) {
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

