#!/bin/bash

usage() {
  echo "Usage: $0 <repository_name> <mvn|gradle|npm> <git_pull_yes|git_pull_no> <path_to_repo>"
  exit 1
}

repo_name=$1
build_type=$2
git_pull=$3
path_to_repo=$4

if [ -z "$repo_name" ] || [ -z "$build_type" ] || [ -z "$path_to_repo" ]; then
  usage
fi

working_dir="$path_to_repo/$repo_name"
if [ ! -d "$working_dir" ]; then
  echo "Error: repository not found: $working_dir"
  exit 1
fi

cd "$working_dir" || exit 1
path=$(pwd)
echo "working_dir: $path"

if [ "$git_pull" = "git_pull_yes" ]; then
  git checkout develop
  git pull
fi

case "$build_type" in
  mvn)
    mvn -DskipTests=true -f "$path"/pom.xml clean install
    ;;
  gradle)
    ./gradlew clean build -x test
    ;;
  npm)
    npm install
    npm run build
    ;;
  *)
    echo "Error: unknown build type '$build_type' (expected mvn|gradle|npm)"
    exit 1
    ;;
esac

if [ $? -ne 0 ]; then
  echo "Error: The command failed."
  exit 1
fi
