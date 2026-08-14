Here is a complete, step-by-step guide to verify that your Observability pipeline (**Metrics, Tracing, and Structured Logging**) is fully working and how to visualize everything inside Grafana.

---

## 1. Prerequisites Check

Before testing, verify the following are up and running:

1. **Docker Infrastructure:**
```bash
docker compose -f docker-compose-infra.yml --env-file env up -d

```


2. **Spring Boot Apps in IntelliJ:**
* Both `GatewayServiceApplication` (Port `8080`) and `KnowledgeServiceApplication` (Port `8081`) are running.
* VM Options / Environment Variables passed in IntelliJ Run Configurations:
```bash
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://localhost:9411/api/v2/spans

```





---

## 2. Verify Observability Pipeline (Step-by-Step)

### Step 1: Generate Test Traffic

Trigger HTTP calls through your Gateway to generate traces, logs, and metrics:

```bash
# Call endpoints via Gateway
curl -X GET http://localhost:8080/api/questions -H "X-User-ID: user-123"
curl -X POST http://localhost:8080/api/questions -H "Content-Type: application/json" -d '{"title":"Test Question"}'

```

---

### Step 2: Check Metrics (Prometheus Endpoint & Scrape)

1. **Check Raw Spring Metrics:**
   Open your browser to:
* [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)
* [http://localhost:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus)
* *Success:* You should see plain text output starting with `# HELP` and `# TYPE` with JVM and HTTP metrics (`http_server_requests_seconds_count`).


2. **Check Prometheus Targets:**
* Open **Prometheus Target Status**: [http://localhost:9090/targets](http://localhost:9090/targets)
* *Success:* Look for your job (`ti-platform-services`). You should see `host.docker.internal:8080` and `host.docker.internal:8081` with status **`UP`** (Green).



---

### Step 3: Check Distributed Tracing (Zipkin UI)

1. Open **Zipkin Web UI**: [http://localhost:9411](http://localhost:9411)
2. Click **Run Query** in the search bar.
3. *Success:*
* You will see trace cards matching your `curl` requests.
* Clicking on a trace shows the **span hierarchy**: `gateway-service` calling `knowledge-service` with timing durations.



---

### Step 4: Check Logs (Loki Pipeline)

Since apps run locally in IntelliJ, they output logs to standard stdout. To send logs to Loki from IntelliJ, either:

* Rely on standard structured JSON formatting (`logging.structured.format.console=ecs`).
* Or verify via Grafana directly in the steps below.

---

## 3. How to View Dashboards in Grafana

1. Open **Grafana UI**: [http://localhost:3000](http://localhost:3000)
2. **Login Credentials:**
* **Username:** `admin`
* **Password:** `admin` (or the value set in `GRAFANA_ADMIN_PASS`)



---

### Method A: Explore Data Instantly (No Setup Required)

Click on **Explore** (compass icon on the left sidebar):

#### 1. View Prometheus Metrics:

* Select **Prometheus** as the datasource at the top.
* In the Metric browser, type:
```promql
http_server_requests_seconds_count

```


* Click **Run Query** to see live HTTP request graphs.

#### 2. Correlate Logs & Traces (Loki / Zipkin):

* Select **Loki** as the datasource.
* Query logs containing your service name or trace ID:
```logql
{app="ti-knowledge-api"} |= "Question created"

```


* Clicking on any log line containing `traceId` renders a direct **Trace link** pointing to the exact span in Zipkin/Tempo.

---

### Method B: Import Ready-Made Spring Boot Dashboards

Rather than building panels from scratch, Grafana has built-in community dashboards designed for Spring Boot Actuator:

1. In Grafana, click the **`+` (Plus)** icon in the top right $\rightarrow$ **Import Dashboard**.
2. Type **Dashboard ID `19004**` (Spring Boot 3 Statistics) or **`14430`** (JVM Micrometer) into the ID box.
3. Click **Load**.
4. Select **Prometheus** as your data source dropdown.
5. Click **Import**.

#### What You Will See on the Dashboard:

* **HTTP:** Total requests, 2xx/4xx/5xx response rates, active connections, and latency (p95, p99).
* **JVM:** Heap & Non-heap memory usage, Garbage Collection pause times, open threads, and CPU load.
* **Database / HikariCP:** Connection pool active/idle counts and wait times.

---

## 4. Quick Verification Summary Matrix

| Visual Check | URL | Expected Outcome |
| --- | --- | --- |
| **Prometheus Targets** | `http://localhost:9090/targets` | `host.docker.internal` targets are **UP (Green)** |
| **Zipkin Tracing** | `http://localhost:9411` | Traces appear spanning across gateway and knowledge services |
| **Grafana UI** | `http://localhost:3000` | Logged in as `admin`, datasources (Prometheus/Loki/Zipkin) are green |
| **Spring Dashboard** | Grafana Dashboard `19004` | Live graphs for JVM Heap, CPU usage, and HTTP status codes |