#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
SRC_DIR="$REPO_ROOT/platform/secrets/templates"
DST_DIR="$REPO_ROOT/platform/secrets/generated"

if [ -d "$DST_DIR" ]; then
  echo "$DST_DIR already exists; refusing to overwrite"
  exit 1
fi

cp -R "$SRC_DIR" "$DST_DIR"

echo "Created $DST_DIR"
echo "Edit secret values before deployment:"
echo "  - work-pool-shared-secret.yaml"
echo "  - work-pool-user-service-secret.yaml"
echo "  - work-pool-task-service-secret.yaml"
echo "  - work-pool-notification-service-secret.yaml"
echo "  - work-pool-payment-service-secret.yaml"
echo "  - work-pool-rating-service-secret.yaml"

