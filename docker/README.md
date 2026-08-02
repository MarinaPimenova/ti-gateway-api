# Run Application

## Run only Gateway with Redis 

```shell
cd docker
docker compose -f _02_redis-service.yaml up
```

Then run `ti-gateway-api` from IntelliJ IDEA.

Check version using the following URL from browser:
`http://localhost:8080/version`

Check authentication via Okta:
`http://localhost:8080`


## Run All microservices with all infrastructure
1. COnfigure env file under docker folder.
   Using an `env` file lets you to use the same file for use by a plain docker <br/>
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
./deploy-all-local.sh PATH_TO_REPOSITORY
cd ../docker
./run-compose.sh
where `env` should contain all values for environment variables
```

where `run-compose.sh` executes the following command
`docker compose -f _03_all_services.yaml --env-file env up`


