#!/bin/bash

path_to_repo=$1

# remove all containers
docker rm -v -f $(docker ps -qa)
docker ps -qa
# $HOME/sp-projects/ms-2026
# $0 <repository_name> <tag> <container_name> <path_to_repo>
./build-docker-image.sh ti-ui dashboard "$path_to_repo"

./build-docker-image.sh ti-gateway-api gateway "$path_to_repo"

./build-docker-image.sh ti-knowledge-api knowledge "$path_to_repo"

./build-docker-image.sh ti-orchestrator-api orchestrator "$path_to_repo"


./build-docker-image.sh ti-import-api import "$path_to_repo"

./build-docker-image.sh ti-export-api export "$path_to_repo"

./build-docker-image.sh ti-ai-orchestrator-api ai-orchrstarator "$path_to_repo"

