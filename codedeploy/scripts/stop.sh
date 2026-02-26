#!/bin/bash
# stop.sh
set -euo pipefail
docker rm -f chat-api || true