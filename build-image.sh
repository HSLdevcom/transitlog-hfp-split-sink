#!/bin/bash

set -e

ORG=${ORG:-hsldevcom}
DOCKER_TAG=${DOCKER_TAG:-edge}
DOCKER_IMAGE=$ORG/transitlog-hfp-split-sink:${DOCKER_TAG}

docker build -t "$DOCKER_IMAGE" .

docker login
docker push "$DOCKER_IMAGE"
