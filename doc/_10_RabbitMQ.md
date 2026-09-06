# Spring Boot RabbitMQ Integration

## Overview

This integration shows how to:

* Configure RabbitMQ in a Spring Boot application
* Publish messages to a RabbitMQ exchange
* Route messages through queues and exchanges
* Consume messages asynchronously using Spring AMQP
* Run RabbitMQ locally using Docker Compose

## Prerequisites

Before running the project, ensure you have the following installed:

* Java 21 (or your project's Java version)
* Maven 3.9+
* Docker Desktop
* Docker Compose (`docker compose` command)

## Running RabbitMQ with Docker

Create and start RabbitMQ:

```bash
docker compose up -d
```

The Docker Compose configuration starts RabbitMQ with the Management plugin enabled.

### RabbitMQ Management Console

* URL: http://127.0.0.1:15672
* Username: `rmuser`
* Password: `rmpassword`

### Ports

| Port  | Description                             |
| ----- | --------------------------------------- |
| 5672  | AMQP protocol port used by applications |
| 15672 | RabbitMQ Management UI                  |



1. Use a **specific RabbitMQ version** rather than `rabbitmq:4-management`.
2. Use RabbitMQ's current `definitions.import_backend` / `definitions.local.path` mechanism instead of `management.load_definitions`. RabbitMQ 4.1 recommends boot-time definition import directly through the core. ([rabbitmq.com][1])
3. Add **both import and upload topology** to `definitions.json`.
4. Add a healthcheck so your Spring Boot services can depend on a healthy broker.
5. For local development, `${RABBITMQ_USER}` / `${RABBITMQ_PASS}` are fine. RabbitMQ explicitly documents these environment variables as suitable for development/CI. ([rabbitmq.com][2])

## Recommended structure

I would use:

```text
docker-compose.yml
rabbitmq/
├── definitions.json
└── rabbitmq.conf
```

### 1. `docker-compose.yml`

```yaml
services:
  rabbitmq:
    container_name: ti-rabbitmq
    image: rabbitmq:4.1-management
    hostname: rabbitmq
    restart: unless-stopped

    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER:-guest}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASS:-guest}

    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
      - ./rabbitmq/definitions.json:/etc/rabbitmq/definitions.json:ro
      - ./rabbitmq/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro

    ports:
      - "5672:5672"
      - "15672:15672"

    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 20s

volumes:
  rabbitmq-data:
```

RabbitMQ's official documentation uses the management image with ports `5672` and `15672` for AMQP and the management UI respectively. ([rabbitmq.com][3])

I prefer `4.1-management` here because your target is RabbitMQ 4.1.x. If you specifically want to track the latest RabbitMQ 4.x release, `rabbitmq:4-management` is also valid, but it makes local environments less reproducible.

---

# 2. `rabbitmq/rabbitmq.conf`

I recommend changing your current configuration to:

```properties
definitions.import_backend = local_filesystem
definitions.local.path = /etc/rabbitmq/definitions.json
definitions.skip_if_unchanged = true
```

This is the current RabbitMQ 4.1 approach for boot-time definition import. ([rabbitmq.com][1])

So I would **not** use:

```properties
management.load_definitions = /etc/rabbitmq/definitions.json
```

Although `management.load_definitions` is still documented for the management plugin, RabbitMQ 4.1 supports importing definitions directly through the core, without depending on the management plugin. ([rabbitmq.com][1])

---

The topology:

```text
                         ┌─────────────────────────────┐
                         │       ti.import             │
                         │        topic exchange       │
                         └──────────────┬──────────────┘
                                        │
                   ┌────────────────────┼────────────────────┐
                   │                    │                    │
          import.requested      import.completed       import.failed
                   │                    │                    │
                   ▼                    ▼                    ▼
        import-worker.import   import-worker.completed   import-worker.fail
                   │                    │                    │
                   │                    │                    │
              Import Worker        Orchestrator          Orchestrator
```

And:

```text
                         ┌─────────────────────────────┐
                         │       ti.upload             │
                         │        topic exchange       │
                         └──────────────┬──────────────┘
                                        │
                   ┌────────────────────┼────────────────────┐
                   │                    │                    │
           upload.requested      upload.completed       upload.failed
                   │                    │                    │
                   ▼                    ▼                    ▼
        upload-worker.import   upload-worker.completed   upload-worker.fail
                   │                    │                    │
                   │                    │                    │
              Upload Worker         Orchestrator          Orchestrator
```

This separation is consistent with the design you described: the worker consumes only its request queue and publishes completion/failure events to separate queues.

---

# 4. `.env`

For local development I would add:

```properties
RABBITMQ_USER=ti
RABBITMQ_PASS=ti
```

I would avoid using `guest/guest` even locally if your Spring applications connect through Docker networking, because RabbitMQ restricts the default `guest` account to local connections by default. ([rabbitmq.com][4])

For example:

```properties
RABBITMQ_USER=ti
RABBITMQ_PASS=ti-dev-password
```

Then your Spring Boot configuration can use:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: ${RABBITMQ_USER:ti}
    password: ${RABBITMQ_PASS:ti-dev-password}
    virtual-host: /
```

If the Spring Boot application itself runs **inside the same Docker Compose network**, use:

```yaml
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: ${RABBITMQ_USER:ti}
    password: ${RABBITMQ_PASS:ti-dev-password}
    virtual-host: /
```

The important distinction is:

```text
Spring Boot on host       → localhost:5672
Spring Boot in Docker     → rabbitmq:5672
```

---

# 5. One important issue with your persistent volume

There is a subtle point with:

```yaml
- rabbitmq-data:/var/lib/rabbitmq
```

RabbitMQ stores its metadata—including users, vhosts, exchanges, queues and bindings—in its internal metadata store. ([rabbitmq.com][5])

Therefore, after the first successful startup, changing `definitions.json` does **not** mean your existing broker will necessarily look like a completely fresh installation.

For a local development environment, when you make a significant topology change, you can reset RabbitMQ with:

```bash
docker compose down -v
docker compose up -d
```

The `-v` is important because it removes:

```text
rabbitmq-data
```

and therefore gives you a fresh RabbitMQ node.

Then check:

```bash
docker compose logs -f rabbitmq
```

You should see RabbitMQ start successfully and import the definitions.

---

# 6. Verify the broker

After startup:

```bash
docker compose ps
```

You should see something similar to:

```text
NAME          IMAGE                     STATUS
ti-rabbitmq   rabbitmq:4.1-management   Up (healthy)
```

Check RabbitMQ itself:

```bash
docker exec ti-rabbitmq rabbitmq-diagnostics ping
```

Expected:

```text
Ping succeeded
```

Then open:

[RabbitMQ Management UI](http://localhost:15672)

Login with:

```text
username: ti
password: ti-dev-password
```

You should see:

```text
Exchanges
├── ti.import
└── ti.upload

Queues
├── import-worker.import
├── import-worker.completed
├── import-worker.fail
├── upload-worker.import
├── upload-worker.completed
└── upload-worker.fail
```

## Useful Commands

```bash
docker compose ps
docker compose logs -f rabbitmq
docker exec -it ti-rabbitmq rabbitmqctl list_users
docker exec -it ti-rabbitmq rabbitmqctl list_queues
docker exec -it ti-rabbitmq rabbitmqctl list_exchanges
```
---

## 7. One architectural recommendation

I would keep your topology exactly as **pre-created infrastructure**, 
rather than having Spring Boot declare these queues.

That means your applications should **consume/publish using existing names**, 
but should not own the topology lifecycle.

For example:

```text
ti-orchestrator-api
       │
       │ publish
       │ import.requested
       ▼
  ti.import exchange
       │
       ▼
import-worker.import
       │
       │ consume
       ▼
ti-import-worker
       │
       ├── publish import.completed ──► import-worker.completed
       │
       └── publish import.failed ─────► import-worker.fail
```

This is particularly appropriate for your architecture because you already have a clearly defined **Orchestrator → Worker → Completion/Failure** workflow.

### One thing I would change later

For production, I would consider making the worker request queues **quorum queues** rather than classic queues, especially for important import/upload jobs. RabbitMQ 4.x supports quorum queues and they provide stronger durability/failure characteristics.

For your **local Docker environment**, however, the durable queues you've defined are perfectly reasonable and keep the setup simple.

Also, RabbitMQ recommends definition import as a deployment-time mechanism for preconfiguring vhosts, users, permissions and topology, so your overall `definitions.json` approach is a good fit. ([rabbitmq.com][1])

**My recommended final setup is therefore:**

```text
docker-compose.yml
        │
        ├── rabbitmq:4.1-management
        ├── 5672
        ├── 15672
        ├── persistent volume
        │
        └── rabbitmq/
             ├── rabbitmq.conf
             └── definitions.json
```

with `rabbitmq.conf` using `definitions.import_backend = local_filesystem` and the complete six-queue topology above.

[1]: https://www.rabbitmq.com/docs/definitions?utm_source=chatgpt.com "Schema Definition Export and Import | RabbitMQ"
[2]: https://www.rabbitmq.com/docs/configure?utm_source=chatgpt.com "Configuration | RabbitMQ"
[3]: https://www.rabbitmq.com/docs/4.1/download?utm_source=chatgpt.com "Installing RabbitMQ | RabbitMQ"
[4]: https://www.rabbitmq.com/docs/access-control?utm_source=chatgpt.com "Authentication, Authorisation, Access Control | RabbitMQ"
[5]: https://www.rabbitmq.com/docs/4.1/metadata-store?utm_source=chatgpt.com "Metadata store | RabbitMQ"

Yes — I can see the problem. There are **two errors** in this `definitions.json`.

---

## One complication: generating the hash

Because your current container has no vhost/user, you can simply generate the hash with a temporary RabbitMQ container, or use the current container's `rabbitmqctl hash_password`.

Try:

```bash
docker exec -it ti-rabbitmq rabbitmqctl hash_password 'admin'
```

Then replace:

```json
"password_hash": "admin"
```

with the generated value.

---

## Then recreate RabbitMQ

Since this is your local development environment:

```bash
docker compose down -v
```

Then:

```bash
docker compose up -d
```

Check:

```bash
docker exec -it ti-rabbitmq rabbitmqctl list_vhosts
```

You should see:

```text
Listing vhosts ...
/
```

Then:

```bash
docker exec -it ti-rabbitmq rabbitmqctl list_users
```

Expected:

```text
Listing users ...
admin    [administrator]
```

Then:

```bash
docker exec -it ti-rabbitmq rabbitmqctl list_permissions -p /
```

Expected:

```text
user    configure    write    read
admin   .*           .*       .*
```

And your management UI should work at:

**[http://localhost:15672](http://localhost:15672)**

with:

```text
Username: admin
Password: admin
```

### One more recommendation

For your local setup, I would use `admin` consistently everywhere:

```text
RabbitMQ user:       admin
RabbitMQ password:   admin
Virtual host:        /
```

and Spring Boot:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin
    virtual-host: /
```

For production, obviously use a non-admin application user and a secret-managed password rather than `admin/admin`.
