package com.example.absurdvalues;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stands in for the slow external host
 */
@RestController
@RequestMapping("/downstream")
public class DownstreamController {

    private final AtomicLong minDelayMs;
    private final AtomicLong maxDelayMs;
    private final AtomicReference<Double> errorRate;

    public DownstreamController(@Value("${repro.downstream.min-delay-ms:8000}") long minDelayMs,
                                 @Value("${repro.downstream.max-delay-ms:10000}") long maxDelayMs,
                                 @Value("${repro.downstream.error-rate:0.0}") double errorRate) {
        this.minDelayMs = new AtomicLong(minDelayMs);
        this.maxDelayMs = new AtomicLong(maxDelayMs);
        this.errorRate = new AtomicReference<>(errorRate);
    }

    @PostMapping("/slow")
    public ResponseEntity<String> slowPost(@RequestBody(required = false) List<String> vendorEntries) throws InterruptedException {
        long delay = randomDelay();
        Thread.sleep(delay);
        boolean rateLimited = ThreadLocalRandom.current().nextDouble() < errorRate.get();
        if (rateLimited) {
            return ResponseEntity.status(429).body("rate limited after " + delay + "ms");
        }
        return ResponseEntity.ok("ok after " + delay + "ms, entries=" + (vendorEntries == null ? 0 : vendorEntries.size()));
    }

    @GetMapping("/slow")
    public ResponseEntity<String> slowGet() throws InterruptedException {
        long delay = randomDelay();
        Thread.sleep(delay);
        return ResponseEntity.ok("ok after " + delay + "ms");
    }

    @PostMapping("/config")
    public String config(@RequestParam(required = false) Long minDelayMs,
                          @RequestParam(required = false) Long maxDelayMs,
                          @RequestParam(required = false) Double errorRate) {
        if (minDelayMs != null) this.minDelayMs.set(minDelayMs);
        if (maxDelayMs != null) this.maxDelayMs.set(maxDelayMs);
        if (errorRate != null) this.errorRate.set(errorRate);
        return "minDelayMs=" + this.minDelayMs.get()
                + " maxDelayMs=" + this.maxDelayMs.get()
                + " errorRate=" + this.errorRate.get();
    }

    private long randomDelay() {
        long min = minDelayMs.get();
        long max = Math.max(min, maxDelayMs.get());
        return min == max ? min : ThreadLocalRandom.current().nextLong(min, max + 1);
    }
}
