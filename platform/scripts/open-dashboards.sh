#!/usr/bin/env bash
set -euo pipefail

ENABLE_MESH=false
for arg in "$@"; do
  case "$arg" in
    --mesh) ENABLE_MESH=true ;;
  esac
done

if [ "$ENABLE_MESH" = "true" ]; then
  kubectl -n istio-system port-forward svc/kiali 20001:20001 >/tmp/pf-kiali.log 2>&1 &
  kubectl -n istio-system port-forward svc/prometheus 19090:9090 >/tmp/pf-prom.log 2>&1 &
  #kubectl -n istio-system port-forward svc/jaeger-query 16686:16686 >/tmp/pf-jaeger.log 2>&1 &
  #kubectl -n istio-system port-forward svc/loki 13100:3100 >/tmp/pf-loki.log 2>&1 &
  echo "Dashboards available:"
  echo "- Kiali:      http://127.0.0.1:20001"
  echo "- Prometheus: http://127.0.0.1:19090"
  #echo "- Jaeger:     http://127.0.0.1:16686"
  #echo "- Loki API:   http://127.0.0.1:13100/ready"
else
  echo "Mesh not enabled. Port-forwarding application services directly:"
  kubectl -n work-pool port-forward svc/work-pool-api-gateway 8080:8080 >/tmp/pf-api.log 2>&1 &
  kubectl -n work-pool port-forward svc/work-pool-ui 3000:80 >/tmp/pf-ui.log 2>&1 &
  kubectl -n work-pool port-forward svc/mailhog 8025:8025 >/tmp/pf-mailhog.log 2>&1 &
  echo "- API Gateway: http://127.0.0.1:8080"
  echo "- UI:          http://127.0.0.1:3000"
  echo "- MailHog:     http://127.0.0.1:8025"
fi

echo "Use 'pkill -f port-forward' to stop forwards"
