#!/bin/bash
set -uo pipefail

usage() {
  echo "Usage: $0 <path-to-repository>"
  exit 1
}

path_to_repo=${1:-}
if [ -z "$path_to_repo" ]; then
  usage
fi

# repo_name build_type(mvn|gradle|npm) git_pull(git_pull_yes|git_pull_no)
# Keep in sync with doc/_06_LOCAL_SETUP.md and docker/docker-compose-full.yml
targets=(
  "ti-gateway-api mvn git_pull_no"
  "ti-knowledge-api gradle git_pull_no"
  "ti-orchestrator-api gradle git_pull_no"
  "ti-import-worker gradle git_pull_no"
  "ti-export-api gradle git_pull_no"
  "ti-ai-orchestrator-api gradle git_pull_no"
  "ti-document-worker gradle git_pull_no"
  "ti-document-agent gradle git_pull_no"
  "ti-sql-agent gradle git_pull_no"
  "ti-knowledge-ui npm git_pull_no"
  "ti-ai-chatbot-ui npm git_pull_no"
  "ti-ai-question-ui npm git_pull_no"
)

failed=()
for target in "${targets[@]}"; do
  read -r repo_name build_type git_pull <<< "$target"
  echo "=== Building target: $repo_name ($build_type) ==="
  if ./build-target.sh "$repo_name" "$build_type" "$git_pull" "$path_to_repo"; then
    echo "OK: $repo_name"
  else
    echo "WARN: $repo_name build failed, skipping"
    failed+=("$repo_name")
  fi
  echo
done

if [ ${#failed[@]} -gt 0 ]; then
  echo "Build failed for: ${failed[*]}"
  exit 1
fi

echo "All targets built successfully."
