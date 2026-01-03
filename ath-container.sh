#!/usr/bin/env bash
set -uo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"

# Obtain the group ID to grant to access the Docker socket
if [[ -z ${DOCKER_GID:-} ]]; then
	DOCKER_GID=$(docker run --rm -v /var/run/docker.sock:/var/run/docker.sock:Z ubuntu:noble stat -c %g /var/run/docker.sock) || exit 1
	export DOCKER_GID
fi

"${DIR}/build-image.sh" || exit 1

trap 'docker-compose kill && docker-compose down' EXIT

docker-compose run --name mvn --rm -P -v "${HOME}/.m2/repository:/home/ath-user/.m2/repository:Z" mvn bash -c 'set-java.sh 25; bash'
status=$?

exit $status
