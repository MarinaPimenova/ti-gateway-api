#!/bin/bash

path_to_repo=$1

# remove all containers
docker rm -v -f $(docker ps -qa)
docker ps -qa
# $HOME/sp-projects/ms-2026

./build-docker-image.sh ti-ui dashboard ti-frontend "$path_to_repo"

./build-docker-image.sh ti-gateway-api gateway ti-gateway-service "$path_to_repo"

./build-docker-image.sh ti-knowledge-api knowledge ti-knowledge-service "$path_to_repo"

./build-docker-image.sh ti-orchestrator-api orchestrator lp-orchestrator-service "$path_to_repo"


./build-docker-image.sh ti-import-api import lp-import-service "$path_to_repo"

./build-docker-image.sh ti-export-api export lp-export-service "$path_to_repo"

./build-docker-image.sh ti-notification-api notification lp-notification-service "$path_to_repo"

