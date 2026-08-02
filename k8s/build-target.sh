#!/bin/bash

usage() {
  echo "Usage: $0 <repository_name> <command_to_exec>"
  exit 1
}

repo_name=$1
command_to_exec=$2
git_pull=$3
path_to_repo=$4

if [ -z "$repo_name" ]; then
  usage
fi

export JAVA_HOME="/c/Program Files/Amazon Corretto/jdk21.0.11_10"
echo $JAVA_HOME
export PATH="${JAVA_HOME}/bin:${PATH}"

working_dir="$path_to_repo/$repo_name"
cd "$working_dir"
path=$(pwd)
echo "working_dir: $path"
if [ "$git_pull" = "git_pull_yes" ]; then
  git checkout develop
  git pull
fi

#mvn -f "$path"/pom.xml clean install
# mvn -DskipTests=true  clean install
if [ "$command_to_exec" = "mvn" ]; then
  mvn -DskipTests=true -f "$path"/pom.xml clean install
else
  echo "$command_to_exec"
  #$("$command_to_exec")
  npm run build
fi

if [ $? -ne 0 ]; then
  echo "Error: The command failed."
  exit 1
fi
