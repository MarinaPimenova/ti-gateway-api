# Run Application

## Run only Redis 

```shell
cd docker
docker compose -f _02_redis-service.yaml up
```

Then run `ti-gateway-api` from IntelliJ IDEA.

Check version using the following URL from browser:
`http://localhost:8080/version`

Check authentication via Okta:
`http://localhost:8080`

## Run Infrastructure Only

> Use Case: Run this when developing locally inside IntelliJ IDEA. 
> All database, messaging, and observability backends will run in Docker, 
> while your Spring Boot applications run locally on your host machine.

### Running IntelliJ Setup:
```shell
docker compose -f docker-compose-infra.yml --env-file env up -d
```
> In IntelliJ, run GatewayServiceApplication 
> and KnowledgeServiceApplication with MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://localhost:9411/api/v2/spans.
---

## Run All microservices with all infrastructure

> Use Case: Production-like deployment running everything inside Docker containers, 
> including Spring Boot microservices with Loki logging drivers attached.

1. Configure env file under docker folder.
   Using an `env` file lets you use the same file for use by a plain docker <br/>
   run --env-file ... command, <br/>
   or to share the same `env` file within multiple services <br/>
   without the need to duplicate a long environment YAML block.

where `env` should **have** the following **structure**
```text
VARIABLE_NAME=VALUE
...
```
2. Run all microservice
**Prerequisites**<br/>
Images for services should be built:<br/>
- FRONTEND service: ti-ui-local:latest <br/>
- GATEWAY service: ti-gateway-local:latest <br/>
- Knowledge service: ti-knowledge-local:latest <br/>

```shell
cd k8s
./build-all-target-and-image.sh PATH_TO_REPOSITORY
cd ../docker
./run-compose.sh
where `env` should contain all values for environment variables
```

where `run-compose.sh` executes the following command
`docker compose -f docker-compose-full.yaml --env-file env up`


