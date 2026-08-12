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

## Docker Compose Configuration

```yaml
services:
  rabbitmq:
    image: rabbitmq:3.10.7-management
    hostname: rabbitmq
    restart: always
    environment:
      - RABBITMQ_DEFAULT_USER=rmuser
      - RABBITMQ_DEFAULT_PASS=rmpassword
      - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbit log_levels [{connection,error},{default,error}] disk_free_limit 2147483648
    ports:
      - "5672:5672"
      - "15672:15672"
```

> **Note:** For Windows + WSL environments, using Docker named volumes is recommended instead of bind mounting RabbitMQ data directories to avoid file permission issues with `.erlang.cookie`.

## Spring Boot Configuration

Configure RabbitMQ connectivity in `src/main/resources/application.yml`:

```yaml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: rmuser
    password: rmpassword
```

> **Important:** YAML requires a space after each colon (`:`). Incorrect formatting may prevent Spring Boot from loading the RabbitMQ properties.

## Application Architecture

```text
Producer
   |
   v
Exchange
   |
Routing Key
   |
   v
Queue
   |
   v
Consumer
```

### Components

* **Producer** – Sends messages to RabbitMQ.
* **Exchange** – Receives messages from producers and routes them.
* **Queue** – Stores messages until they are processed.
* **Consumer** – Listens to queues and processes incoming messages.

## Running the Application

Start RabbitMQ:

```bash
docker compose up -d
```

Build the application:

```bash
./mvnw clean package
```

Run the application:

```bash
./mvnw spring-boot:run
```

Or:

```bash
java -jar target/<application-name>.jar
```

## Verifying the Setup

1. Start RabbitMQ.
2. Start the Spring Boot application.
3. Open the RabbitMQ Management UI.
4. Navigate to:

    * Exchanges
    * Queues
    * Connections
    * Channels
5. Publish a message from the application.
6. Verify that the message is routed and consumed successfully.

## Troubleshooting

### `docker-compose: command not found`

Use:

```bash
docker compose up -d
```

instead of:

```bash
docker-compose up -d
```

### `ACCESS_REFUSED - Login was refused`

Verify:

* RabbitMQ container is running
* Credentials in `application.yml` match RabbitMQ credentials
* RabbitMQ user exists
* `application.yml` formatting is valid

### `.erlang.cookie must be accessible by owner only`

This usually occurs on Windows bind mounts. Prefer Docker named volumes or a WSL-native Linux directory.

## Useful Commands

```bash
docker compose ps
docker compose logs -f rabbitmq
docker exec -it rabbitmq-1 rabbitmqctl list_users
docker exec -it rabbitmq-1 rabbitmqctl list_queues
docker exec -it rabbitmq-1 rabbitmqctl list_exchanges
```

## References

* [RabbitMQ: Terminology and Basic Entities](https://habr.com/en/companies/slurm/articles/703060/)
* [Spring Boot - RabbitMQ Configuration](https://www.geeksforgeeks.org/springboot/spring-boot-rabbitmq-configuration/)
* [How To RUn RabbitMQ in Docker](https://habr.com/en/companies/slurm/articles/704208/)
* Spring AMQP Documentation
* RabbitMQ Documentation
* GeeksforGeeks: Spring Boot RabbitMQ Configuration
