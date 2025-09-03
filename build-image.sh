#!/usr/bin/env bash
set -uo pipefail

uid=$(id -u) || exit 1
gid=$(id -g) || exit 1

# high chance of uid / group already existing in the container
# known to happen on macOS
if ((uid < 1000)); then
	uid=1001
fi

if ((gid < 1000)); then
	gid=1001
fi

if [[ -z ${BROWSER:-} ]]; then
	export BROWSER=firefox
fi

# Obtain the group ID to grant to access the Docker socket
if [[ -z ${DOCKER_GID:-} ]]; then
	DOCKER_GID=$(docker run --rm -v /var/run/docker.sock:/var/run/docker.sock:Z ubuntu:noble stat -c %g /var/run/docker.sock) || exit 1
	export DOCKER_GID
fi

docker-compose pull || exit 1
docker-compose build --build-arg=uid="$uid" --build-arg=gid="$gid" || exit 1

exit 0
