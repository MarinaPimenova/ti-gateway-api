#!/bin/bash

path_to_repo=$1

# ti-ui
./build-target.sh ti-ui 'npm run build' git_pull_no "$path_to_repo"
# gateway git_pull_yes
./build-target.sh ti-gateway-api mvn git_pull_no "$path_to_repo"

./build-target.sh ti-knowledge-api mvn git_pull_no "$path_to_repo"

./build-target.sh ti-orchestrator-api mvn git_pull_no "$path_to_repo"

./build-target.sh ti-import-api mvn git_pull_no "$path_to_repo"

./build-target.sh ti-export-api mvn git_pull_no "$path_to_repo"

# notification
./build-target.sh ti-notification-api mvn git_pull_no "$path_to_repo"



