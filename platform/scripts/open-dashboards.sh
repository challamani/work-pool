#!/usr/bin/env bash
set -euo pipefail

kubectl -n istio-system port-forward svc/kiali 20001:20001 >/tmp/pf-kiali.log 2>&1 &
kubectl -n istio-system port-forward svc/prometheus 19090:9090 >/tmp/pf-prom.log 2>&1 &
kubectl -n istio-system port-forward svc/jaeger-query 16686:16686 >/tmp/pf-jaeger.log 2>&1 &
kubectl -n istio-system port-forward svc/loki 13100:3100 >/tmp/pf-loki.log 2>&1 &

echo "Dashboards available:"
echo "- Kiali:      http://127.0.0.1:20001"
echo "- Prometheus: http://127.0.0.1:19090"
echo "- Jaeger:     http://127.0.0.1:16686"
echo "- Loki API:   http://127.0.0.1:13100/ready"
echo "Use 'pkill -f port-forward' to stop forwards"

