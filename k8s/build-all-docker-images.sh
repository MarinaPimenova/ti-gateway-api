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

# repo_name image_tag  -->  built image is <image_tag>-local, matching docker-compose-full.yml
# Keep in sync with doc/_06_LOCAL_SETUP.md and docker/docker-compose-full.yml
images=(
  "ti-gateway-api ti-gateway"
  "ti-knowledge-api ti-knowledge"
  "ti-orchestrator-api ti-orchestrator"
  "ti-import-worker ti-import-worker"
  "ti-export-api ti-export"
  "ti-ai-orchestrator-api ti-ai-orchestrator"
  "ti-document-worker ti-document-worker"
  "ti-document-agent ti-document-agent"
  "ti-sql-agent ti-sql-agent"
  "ti-knowledge-ui ti-knowledge-ui"
  "ti-ai-chatbot-ui ti-ai-chatbot"
  "ti-ai-question-ui ti-ai-question"
)

failed=()
for image in "${images[@]}"; do
  read -r repo_name image_tag <<< "$image"
  echo "=== Building image: ${image_tag}-local (from $repo_name) ==="
  if ./build-docker-image.sh "$repo_name" "$image_tag" "$path_to_repo"; then
    echo "OK: ${image_tag}-local"
  else
    echo "WARN: ${image_tag}-local build failed, skipping"
    failed+=("$repo_name")
  fi
  echo
done

if [ ${#failed[@]} -gt 0 ]; then
  echo "Image build failed for: ${failed[*]}"
  exit 1
fi

echo "All images built successfully."
