#!/bin/bash
set -euo pipefail
# This is the existing named-volume target; never delete or reformat its content.
mkdir -p /tmp/kraft-combined-logs
chown -R 1000:1000 /tmp/kraft-combined-logs
chmod u+rwx /tmp/kraft-combined-logs
# Only ownership preparation runs as root. Broker and topic tools run as appuser.
exec su-exec appuser:appuser /bin/bash /opt/bootstrap/start-kafka.sh
