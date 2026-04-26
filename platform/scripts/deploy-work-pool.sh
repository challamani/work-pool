#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
BASE_DIR="$REPO_ROOT/platform/k8s/base"
SECRETS_DIR="$REPO_ROOT/platform/secrets/generated"

ENABLE_MESH=false
for arg in "$@"; do
  case "$arg" in
    --mesh) ENABLE_MESH=true ;;
  esac
done

if [ ! -d "$SECRETS_DIR" ]; then
  echo "Missing $SECRETS_DIR. Copy templates and fill values first."
  exit 1
fi

# Toggle mesh resources in kustomization
KUSTOMIZATION="$BASE_DIR/kustomization.yaml"
if [ "$ENABLE_MESH" = "true" ]; then
  if ! grep -q "^  - mesh" "$KUSTOMIZATION"; then
    echo "  - mesh" >> "$KUSTOMIZATION"
  fi
else
  sed -i '' '/^  - mesh$/d' "$KUSTOMIZATION"
fi

kubectl apply -k "$BASE_DIR"
kubectl apply -f "$SECRETS_DIR"
# Re-apply base so workloads pick up secret-backed envFrom references after secrets exist.
kubectl apply -k "$BASE_DIR"

kubectl -n work-pool rollout status deploy/user-service --timeout=300s
kubectl -n work-pool rollout status deploy/task-service --timeout=300s
kubectl -n work-pool rollout status deploy/notification-service --timeout=300s
kubectl -n work-pool rollout status deploy/payment-service --timeout=300s
kubectl -n work-pool rollout status deploy/rating-service --timeout=300s
kubectl -n work-pool rollout status deploy/api-gateway --timeout=300s
kubectl -n work-pool rollout status deploy/work-pool-ui --timeout=300s

echo "Work Pool services deployed to namespace work-pool"
