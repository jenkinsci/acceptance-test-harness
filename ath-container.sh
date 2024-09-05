#!/usr/bin/env bash
# https://disconnected.systems/blog/another-bash-strict-mode/
set -euo pipefail
trap 's=$?; echo "$0: Error $s on line "$LINENO": $BASH_COMMAND"; exit $s' ERR

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"

uid=$(id -u)
gid=$(id -g)
tag='jenkins/ath'
java_version="${java_version:-17}"

# high chance of uid / group already existing in the container
# known to happen on macOS
if ((uid < 1000)); then
	uid=1001
fi

if ((gid < 1000)); then
	gid=1001
fi

docker build \
	--build-arg=uid="$uid" \
	--build-arg=gid="$gid" \
	"$DIR/src/main/resources/ath-container" \
	-t "$tag"

# obtain the groupId to grant to access the docker socket
dockergid=$(docker run --rm -v /var/run/docker.sock:/var/run/docker.sock ubuntu:noble stat -c %g /var/run/docker.sock)

docker run \
	--interactive \
	--tty \
	--rm \
	--publish-all \
	--user ath-user \
	--workdir /home/ath-user/sources \
	--shm-size 2g \
	--group-add ${dockergid} \
	-v /var/run/docker.sock:/var/run/docker.sock \
	-v "$(pwd):/home/ath-user/sources" \
	-v "${HOME}/.m2/repository:/home/ath-user/.m2/repository" \
	$tag \
	/bin/bash -c "set-java.sh $java_version; bash"
