package com.example.absurdvalues;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Repro controller/method
 */
@RestController
public class ReproController {

    private final WebClient webClient;

    public ReproController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/repro/parallel-webclient")
    public List<String> parallelWebClient(@RequestParam(defaultValue = "21") int vendors,
                                           @RequestParam(defaultValue = "2") int entriesPerVendor) {
        Map<String, List<String>> entriesByVendor = new LinkedHashMap<>();
        for (int v = 0; v < vendors; v++) {
            List<String> entry = new java.util.ArrayList<>();
            for (int o = 0; o < entriesPerVendor; o++) {
                entry.add("oli-" + v + "-" + o);
            }
            entriesByVendor.put("vendor-" + v, entry);
        }

        String token = "foo-bar-baz-token";

        return entriesByVendor.values().stream()
                .parallel()
                .map(vendorOlis -> webClient.post()
                        .uri("/downstream/slow")
                        .header("Some-Token", token)
                        .bodyValue(vendorOlis)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block())
                .toList();
    }
}
