# Work Pool Local Platform (Kind + Istio)

This folder contains local-cluster deployment assets for Work Pool.

## Goals

- Deploy backend services to `work-pool` namespace on Kind.
- Enable Istio sidecars for all backend microservices.
- Exclude UI from sidecar injection.
- Install observability components:
  - Metrics: Prometheus
  - Service graph: Kiali
- Use Kiali for service graph and traffic insights.
- Keep secrets out of git via external generated manifests.

## Folder Layout

- `kind/`: optional Kind config (single-node)
- `k8s/base/`: core app + infra manifests
- `secrets/templates/`: secret templates (commit-safe)
- `scripts/`: bootstrap, deploy, and validation scripts

## Prerequisites

- `docker`
- `kubectl`
- `kind`
- `istioctl`
- `python3`

## Quick Start

```bash
bash platform/scripts/bootstrap-kind.sh
sudo cloud-provider-kind --gateway-channel standard
bash platform/scripts/install-mesh-observability.sh
bash platform/scripts/prepare-secrets.sh
# edit files under platform/secrets/generated with real values
bash platform/scripts/build-load-images.sh
bash platform/scripts/deploy-work-pool.sh
bash platform/scripts/validate-e2e.sh
```

`bootstrap-kind.sh` now uses a basic single-node cluster create (`kind create cluster --name work-pool`).
Run `cloud-provider-kind` in a separate terminal and keep it running while using LoadBalancer endpoints.

## Validation Scope

`validate-e2e.sh` verifies:

- mesh sidecar injection for backend services and exclusion for UI
- ingress traffic path through Istio gateway (`api.work-pool.org`)
- Prometheus metric availability (`istio_requests_total` in `work-pool`)

## Dashboards

```bash
bash platform/scripts/open-dashboards.sh
```

This opens local port-forwards for:

- Kiali: `http://127.0.0.1:20001`
- Prometheus: `http://127.0.0.1:19090`

With `cloud-provider-kind` running, `kiali` is patched to `LoadBalancer` and can also be reached via its external address:

```bash
kubectl -n istio-system get svc kiali
```

