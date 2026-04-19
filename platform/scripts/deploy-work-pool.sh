#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
BASE_DIR="$REPO_ROOT/platform/k8s/base"
SECRETS_DIR="$REPO_ROOT/platform/secrets/generated"

if [ ! -d "$SECRETS_DIR" ]; then
  echo "Missing $SECRETS_DIR. Copy templates and fill values first."
  exit 1
fi

kubectl apply -k "$BASE_DIR"
kubectl apply -f "$SECRETS_DIR"
# Re-apply base so workloads pick up secret-backed envFrom references after secrets exist.
kubectl apply -k "$BASE_DIR"

kubectl -n work-pool rollout status deploy/work-pool-user-service --timeout=300s
kubectl -n work-pool rollout status deploy/work-pool-task-service --timeout=300s
kubectl -n work-pool rollout status deploy/work-pool-notification-service --timeout=300s
kubectl -n work-pool rollout status deploy/work-pool-payment-service --timeout=300s
kubectl -n work-pool rollout status deploy/work-pool-rating-service --timeout=300s
kubectl -n work-pool rollout status deploy/work-pool-api-gateway --timeout=300s
kubectl -n work-pool rollout status deploy/work-pool-ui --timeout=300s

echo "Work Pool services deployed to namespace work-pool"
