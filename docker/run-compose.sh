#!/bin/sh

docker-compose down --remove-orphans
docker-compose -f ./_03_all_services.yaml --env-file ./env up