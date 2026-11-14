#!/bin/bash
set -euo pipefail
ready=/tmp/kafka-topics-ready
rm -f "$ready"
broker_pid=""
cleanup() {
  rm -f "$ready"
  if [[ -n "$broker_pid" ]] && kill -0 "$broker_pid" 2>/dev/null; then
    kill -TERM "$broker_pid" 2>/dev/null || true
    wait "$broker_pid" || true
  fi
}
trap cleanup EXIT
trap 'exit 143' TERM
trap 'exit 130' INT
/etc/kafka/docker/run &
broker_pid=$!
connected=false
for ((attempt=1; attempt<=60; attempt++)); do
  if ! kill -0 "$broker_pid" 2>/dev/null; then
    echo "Kafka exited before startup completed" >&2
    exit 1
  fi
  if timeout 8 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:29092 --list >/dev/null 2>&1; then
    connected=true
    break
  fi
  sleep 2
done
if [[ "$connected" != true ]]; then
  echo "Timed out waiting for Kafka broker" >&2
  exit 1
fi
/bin/bash /opt/bootstrap/init-topics.sh
kill -0 "$broker_pid"
touch "$ready"
echo "Kafka broker and required topics are ready"
wait "$broker_pid"
