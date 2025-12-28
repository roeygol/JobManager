package com.jobmanager.orchestrator.config;

import com.jobmanager.orchestrator.application.service.RemoteClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;

import java.time.Duration;

/**
 * Configuration for WebClient used for remote job execution.
 */
@Configuration
public class WebClientConfig {

    private final RemoteClientProperties properties;

    public WebClientConfig(RemoteClientProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.getReadTimeout()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 
                        properties.getConnectTimeout());

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}

