#!/usr/bin/env bash
# https://disconnected.systems/blog/another-bash-strict-mode/
set -euo pipefail
trap 's=$?; echo "$0: Error $s on line "$LINENO": $BASH_COMMAND"; exit $s' ERR

uid=$(id -u)
gid=$(id -g)
tag="jenkins/ath"
java_version="${java_version:-8}"

docker build --build-arg=uid="$uid" --build-arg=gid="$gid" --build-arg=java_version="$java_version" src/main/resources/ath-container -t "$tag"

run_opts="--interactive --tty --rm --publish-all --user ath-user --workdir /home/ath-user/ath-sources"
run_drive_mapping="-v /var/run/docker.sock:/var/run/docker.sock -v $(pwd):/home/ath-user/ath-sources -v ${HOME}/.m2/repository:/home/ath-user/.m2/repository"
docker run --shm-size 2g $run_opts $run_drive_mapping $tag /bin/bash
