#!/usr/bin/env bash
set -euo pipefail

CLUSTER_NAME=${CLUSTER_NAME:-work-pool}

for cmd in docker kubectl kind; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required command: $cmd"
    exit 1
  fi
done

if ! kind get clusters | grep -qx "$CLUSTER_NAME"; then
  kind create cluster --name "$CLUSTER_NAME"
else
  echo "Kind cluster '$CLUSTER_NAME' already exists"
fi

kubectl get namespace work-pool >/dev/null 2>&1 || kubectl create namespace work-pool
kubectl label namespace work-pool istio-injection=enabled --overwrite

echo "Cluster '$CLUSTER_NAME' is ready and namespace 'work-pool' is labeled for Istio injection"
echo "If you need LoadBalancer services on localhost, run in a separate terminal:"
echo "  sudo cloud-provider-kind --gateway-channel standard"

