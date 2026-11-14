#!/bin/bash
set -euo pipefail
# Match the topic names in the five Spring service configurations.
for topic in product.inventory-init.v1 inventory.reserve.requested.v1 inventory.reservation.result.v1 inventory.release.requested.v1 order.shipped.v1 order.delivered.v1 shipping.status.v1; do
  timeout 60 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:29092 --create --if-not-exists --topic "$topic" --partitions 1 --replication-factor 1
  timeout 60 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:29092 --describe --topic "$topic"
done
