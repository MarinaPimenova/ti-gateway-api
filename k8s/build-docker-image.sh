#!/bin/bash

usage() {
  echo "Usage: $0 <repository_name> <tag>"
  exit 1
}

repo_name=$1
tag_arg=$2
container_name=$3
path_to_repo=$4

if [ -z "$tag_arg" ] || [ -z "$repo_name" ]; then
  usage
fi


local_image=$(docker images -q "$tag_arg"-local)
if [ -n "$local_image" ]; then
  docker rmi -f "$local_image"
fi
# $HOME/sp-projects/ms-2026
working_dir="$path_to_repo/$repo_name"
cd "$working_dir"
path=$(pwd)
echo "working_dir: $path"

docker build -t "$tag_arg"-local "$path"

if [ $? -ne 0 ]; then
  echo "Error: The command failed."
  exit 1
fi

