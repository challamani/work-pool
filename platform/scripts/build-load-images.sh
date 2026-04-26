#!/usr/bin/env bash
set -euo pipefail

CLUSTER_NAME=${CLUSTER_NAME:-work-pool}
KIND_NODE=${KIND_NODE:-${CLUSTER_NAME}-control-plane}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

build_backend() {
  local service="$1"
  docker build -t "$service:local" -f "$REPO_ROOT/work-pool-backend/$service/Dockerfile" "$REPO_ROOT/work-pool-backend"
  kind load docker-image "$service:local" --name "$CLUSTER_NAME" --nodes "$KIND_NODE"
}

build_backend user-service
build_backend task-service
build_backend notification-service
build_backend payment-service
build_backend rating-service
build_backend api-gateway

docker build -t work-pool-ui:local "$REPO_ROOT/work-pool-ui" \
  --build-arg VITE_API_BASE_URL=http://api.work-pool.org \
  --build-arg VITE_WS_URL=ws://api.work-pool.org/ws
kind load docker-image work-pool-ui:local --name "$CLUSTER_NAME" --nodes "$KIND_NODE"

echo "All local images built and loaded into kind/$CLUSTER_NAME"

