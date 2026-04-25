#!/usr/bin/env bash
set -euo pipefail

echo "Access Work Pool and observability UIs via virtual hostnames:"
echo "- UI:          http://ui.work-pool.org"
echo "- API Gateway: http://api.work-pool.org"
echo "- Kiali:       http://kiali.work-pool.org/kiali/"
echo "- Prometheus:  http://prometheus.work-pool.org"
echo
echo "OAuth callback host should match gateway routing:"
echo "- http://api.work-pool.org/api/v1/auth/oauth2/callback/google"
echo
echo "Ensure hostnames resolve to the Istio ingress endpoint (DNS or /etc/hosts)."
