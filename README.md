# absurd-values-spring-webclient

Repro app for [newrelic/newrelic-java-agent#3017](https://github.com/newrelic/newrelic-java-agent/issues/3017).

## Controllers

- **`ReproController`** (`POST /repro/parallel-webclient`) — the actual repro trigger. Fans a map of vendor entries out across a parallel stream and calls the downstream host via `WebClient`, blocking with `.block()` on each call from within the parallel stream.
- **`DownstreamController`** (`GET/POST /downstream/slow`, `POST /downstream/config`) — stands in for the slow external host. `/slow` sleeps for a configurable delay before responding; `/config` adjusts that delay (`minDelayMs`/`maxDelayMs`) and error rate at runtime.
- **`JerseyComparisonController`** (`GET /compare/jersey-call`) — a blocking Jersey client call to the same simulated downstream host, used as a control for comparing against the WebClient path.
