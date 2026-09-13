#!/bin/sh

docker-compose -f ./docker-compose-full.yml down --remove-orphans
docker-compose -f ./docker-compose-full.yml --env-file ./env up