#!/usr/bin/env bash
set -uo pipefail

jdk="$1"
browser="$2"
jenkinsVersion="$3"

# Obtain the group ID to grant to access the Docker socket
if [[ -z ${DOCKER_GID:-} ]]; then
	DOCKER_GID=$(docker run --rm -v /var/run/docker.sock:/var/run/docker.sock:Z ubuntu:noble stat -c %g /var/run/docker.sock) || exit 1
	export DOCKER_GID
fi

trap 'docker-compose kill && docker-compose down' EXIT

docker-compose run -e "MAVEN_ARGS=${MAVEN_ARGS}" --name mvn -T --rm -v "${MAVEN_SETTINGS}:${MAVEN_SETTINGS}:Z" mvn bash -s <<-INSIDE
	set-java.sh ${jdk}

	# Ensure that Jenkins node setup does not influence the container Java setup
	unset JAVA_HOME

	java -version
	mvn -v

	run.sh remote-webdriver-${browser} ${jenkinsVersion} -Dmaven.test.failure.ignore=true -DforkCount=1 -B
INSIDE
status=$?

if [[ -d target/surefire-reports ]]; then
	find target/surefire-reports -type f -name 'TEST-*.xml' -print0 |
		xargs -0 sed -i 's!\[\[ATTACHMENT|/home/ath-user/sources/target\(/[^]]*\)\]\]![[ATTACHMENT|'"$PWD"'/target\1]]!g'
else
	echo 'No test results to be saved'
fi

exit $status
