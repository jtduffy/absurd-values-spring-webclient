package com.example.absurdvalues;

import java.time.Duration;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(@Value("${server.port:8080}") int port,
                                @Value("${repro.webclient.max-connections:5}") int maxConnections,
                                @Value("${repro.webclient.pending-acquire-max-count:500}") int pendingAcquireMaxCount,
                                @Value("${repro.webclient.pending-acquire-timeout-seconds:15}") long pendingAcquireTimeoutSeconds) {
        ConnectionProvider provider = ConnectionProvider.builder("repro-pool")
                .maxConnections(maxConnections)
                .pendingAcquireMaxCount(pendingAcquireMaxCount)
                .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeoutSeconds))
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public Client jerseyClient() {
        return ClientBuilder.newClient();
    }
}
