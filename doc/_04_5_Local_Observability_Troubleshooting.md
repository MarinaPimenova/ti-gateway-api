This is the missing piece. Your output shows:

```text
eth0: state DOWN
```

while:

```bash
curl http://localhost:8080/actuator/health
```

works.

That strongly indicates your WSL environment is using **localhost/mirrored networking**, rather than the traditional WSL NAT interface. Therefore, the previous `172.25.0.1` / `10.255.255.254` approach is not the right solution.

### The simplest solution for your local setup

Since Prometheus only needs to scrape applications running on the Windows/WSL host, run the **Prometheus container with host networking**.

Change your `prometheus` service to:

```yaml
prometheus:
  image: prom/prometheus:v2.51.0
  container_name: prometheus
  network_mode: host
  volumes:
    - ./observability/prometheus/prometheus-local.yml:/etc/prometheus/prometheus.yml
```

Remove these from the Prometheus service:

```yaml
ports:
  - "9090:9090"

extra_hosts:
  - "host.docker.internal:host-gateway"

networks:
  - knowledge-network
```

So the complete service becomes:

```yaml
prometheus:
  image: prom/prometheus:v2.51.0
  container_name: prometheus
  network_mode: host
  volumes:
    - ./observability/prometheus/prometheus-local.yml:/etc/prometheus/prometheus.yml
```

### Then change Prometheus configuration

Instead of:

```yaml
scrape_configs:
  - job_name: 'ti-platform-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - 'host.docker.internal:8080'
          - 'host.docker.internal:8081'
```

use:

```yaml
scrape_configs:
  - job_name: 'ti-platform-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - 'localhost:8080'
          - 'localhost:8081'
```

Now the networking is:

```text
                         WSL
                          │
              ┌───────────┴───────────┐
              │                       │
       Prometheus container       Windows/IntelliJ
       network_mode: host         Spring Boot
              │                       │
              └──── localhost ───────┘
                       :8080
                       :8081
```

This avoids the problematic:

```text
Docker container
      ↓
host.docker.internal
      ↓
10.255.255.254
      ↓
connection refused
```

### Restart Prometheus

From your `docker` directory:

```bash
docker compose -f docker-compose-infra.yml down
docker compose -f docker-compose-infra.yml up -d prometheus
```

Check:

```bash
docker ps
```

Then test from the Prometheus container:

```bash
docker exec -it prometheus sh
wget -S -O- http://localhost:8080/actuator/health
```

You should get:

```json
{"groups":["liveness","readiness"],"status":"UP"}
```

And importantly, Prometheus itself should now be available at:

```text
http://localhost:9090
```

### One caveat

`network_mode: host` means Prometheus is no longer attached to your Docker `knowledge-network`. That's perfectly fine **if Prometheus only needs to scrape locally running Spring Boot applications**.

However, later, when you want Prometheus to scrape Docker services such as:

```text
ti-orchestrator-api:8082
ti-import-api:8083
ti-export-api:8084
```

I would recommend a different setup: keep Prometheus on `knowledge-network` and configure the Docker services themselves as scrape targets.

For your **current local development scenario**, though, `network_mode: host` is the cleanest solution given that `localhost:8080` already works directly from WSL.
