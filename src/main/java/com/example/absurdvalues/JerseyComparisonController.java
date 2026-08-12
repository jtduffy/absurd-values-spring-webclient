package com.example.absurdvalues;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.ws.rs.client.Client;

@RestController
public class JerseyComparisonController {

    private final Client jerseyClient;
    private final String baseUrl;

    public JerseyComparisonController(Client jerseyClient, @Value("${server.port:8080}") int port) {
        this.jerseyClient = jerseyClient;
        this.baseUrl = "http://localhost:" + port;
    }

    @GetMapping("/compare/jersey-call")
    public String jerseyCall() {
        return jerseyClient.target(baseUrl)
                .path("/downstream/slow")
                .request()
                .get(String.class);
    }
}
