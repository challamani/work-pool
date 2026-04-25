#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

EXPOSE_KIALI_LB=${EXPOSE_KIALI_LB:-true}

for cmd in kubectl istioctl; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required command: $cmd"
    exit 1
  fi
done

istioctl install --set profile=demo -y

kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.28/samples/addons/prometheus.yaml
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.28/samples/addons/kiali.yaml

if [ "$EXPOSE_KIALI_LB" = "true" ]; then
  # Makes Kiali reachable through cloud-provider-kind without port-forward.
  kubectl -n istio-system patch svc kiali -p '{"spec":{"type":"LoadBalancer"}}'
fi

kubectl -n istio-system rollout status deploy/istiod --timeout=180s
kubectl -n istio-system rollout status deploy/kiali --timeout=180s
kubectl -n istio-system rollout status deploy/prometheus --timeout=180s

echo "Istio mesh + observability stack installed"

