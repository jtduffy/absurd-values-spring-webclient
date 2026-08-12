#!/usr/bin/env bash
# Usage: ./load-test.sh [duration_seconds] [burst_concurrency] [vendors]

set -euo pipefail

HOST="http://localhost:8080"
DURATION=${1:-120}
CONCURRENCY=${2:-5}
VENDORS=${3:-21}

echo "Configuring simulated downstream host to respond in 8-10s..."
curl -sS -X POST "${HOST}/downstream/config?minDelayMs=8000&maxDelayMs=10000" | sed 's/^/  /'
echo ""

echo "Sending ${CONCURRENCY} concurrent requests/burst against /repro/parallel-webclient"
echo "(vendors=${VENDORS}) and /compare/jersey-call for ${DURATION}s. Press Ctrl+C to stop early."
echo ""

END=$((SECONDS + DURATION))
while [ ${SECONDS} -lt ${END} ]; do
  for i in $(seq 1 "${CONCURRENCY}"); do
    curl -sS -o /dev/null -X POST "${HOST}/repro/parallel-webclient?vendors=${VENDORS}" &
    curl -sS -o /dev/null "${HOST}/compare/jersey-call" &
  done
  wait
  echo "burst complete at $((SECONDS))s"
done

echo "Done."
