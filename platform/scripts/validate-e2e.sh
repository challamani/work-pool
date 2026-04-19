#!/usr/bin/env bash
set -euo pipefail

NS=work-pool

kubectl -n "$NS" get pods

# Ensure backend pods have Istio sidecar and UI does not.
for app in work-pool-user-service work-pool-task-service work-pool-notification-service work-pool-payment-service work-pool-rating-service work-pool-api-gateway; do
  containers=$(kubectl -n "$NS" get pod -l app="$app" -o jsonpath='{.items[0].spec.containers[*].name}')
  echo "$app containers: $containers"
  echo "$containers" | grep -q istio-proxy
  echo "Sidecar check passed for $app"
done

ui_containers=$(kubectl -n "$NS" get pod -l app=work-pool-ui -o jsonpath='{.items[0].spec.containers[*].name}')
echo "work-pool-ui containers: $ui_containers"
if echo "$ui_containers" | grep -q istio-proxy; then
  echo "UI pod unexpectedly has istio-proxy sidecar"
  exit 1
fi

kubectl -n istio-system port-forward svc/istio-ingressgateway 18080:80 >/tmp/pf-igw.log 2>&1 &
PF_IGW=$!
trap 'kill ${PF_IGW} >/dev/null 2>&1 || true' EXIT
sleep 3

for i in $(seq 1 20); do
  curl -fsS -H 'Host: api.work-pool.local' http://127.0.0.1:18080/actuator/health >/dev/null
  curl -fsS -H 'Host: api.work-pool.local' http://127.0.0.1:18080/api/v1/tasks >/dev/null || true
done

kubectl -n istio-system port-forward svc/prometheus 19090:9090 >/tmp/pf-prom-val.log 2>&1 &
PF_PROM=$!
sleep 2
METRIC=$(curl -fsS 'http://127.0.0.1:19090/api/v1/query?query=sum(istio_requests_total%7Bdestination_workload_namespace%3D%22work-pool%22%7D)' | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d["data"]["result"][0]["value"][1] if d["data"]["result"] else "0")')
kill ${PF_PROM} >/dev/null 2>&1 || true
echo "Prometheus istio_requests_total sum(work-pool): $METRIC"

kubectl -n istio-system port-forward svc/jaeger-query 16686:16686 >/tmp/pf-jaeger-val.log 2>&1 &
PF_JAEGER=$!
sleep 2
curl -fsS http://127.0.0.1:16686/api/services | python3 -c 'import json,sys; d=json.load(sys.stdin); svcs=d.get("data",[]); print("jaeger services count:", len(svcs)); print("contains istio-proxy traces:", any("istio" in s for s in svcs))'
kill ${PF_JAEGER} >/dev/null 2>&1 || true

kubectl -n istio-system get ds/fluent-bit
kubectl -n istio-system get deploy/loki

echo "E2E validation checks completed"

