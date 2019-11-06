#!/bin/sh
set -u

docker login --username=$DOCKER_USERNAME --password=$DOCKER_PASSWORD
docker push $DOCKER_DEVEL_TAG

