#!/usr/bin/env bash

set -euo pipefail
# set -e
  #Stops execution if any command fails.
# set -u
  #Fails when using an undefined variable.
# set -o pipefail
  #Detects failures in pipelines

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <path-to-repository>"
    exit 1
fi

path_to_repo="$1"

./build-all-targets.sh "$path_to_repo"
./build-all-docker-image.sh "$path_to_repo"