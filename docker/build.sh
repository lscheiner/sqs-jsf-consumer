#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
PROJECT_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"

IMAGE_NAME="aws-localstack-console"
IMAGE_TAG="${1:-latest}"

echo "======================================"
echo "Project : $PROJECT_ROOT"
echo "Image   : ${IMAGE_NAME}:${IMAGE_TAG}"
echo "======================================"

docker build \
    --file "$SCRIPT_DIR/Dockerfile" \
    --tag "${IMAGE_NAME}:${IMAGE_TAG}" \
    "$PROJECT_ROOT"

echo
echo "Build concluído!"