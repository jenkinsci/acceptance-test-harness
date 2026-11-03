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

# withOpenTestReporting listens on the loopback interface of the agent, which the tests cannot reach from the mvn
# container: relay it to the host gateway (host.docker.internal), and from there to the loopback interface of the
# container.
otr_relay=
otr_args=
if [[ -n ${JUNIT_OTR_SOCKET_PORT:-} ]]; then
	gateway=$(docker network inspect bridge --format '{{(index .IPAM.Config 0).Gateway}}') || exit 1
	otr_relay=$(docker run -d --rm --network host --entrypoint socat jenkins/ath:latest "TCP-LISTEN:${JUNIT_OTR_SOCKET_PORT},bind=${gateway},fork,reuseaddr" "TCP:127.0.0.1:${JUNIT_OTR_SOCKET_PORT}") || exit 1
	# capture the output of each test, like Surefire does in its XML reports
	otr_args="-Djunit.platform.reporting.open.xml.enabled=true -Djunit.platform.reporting.open.xml.socket=${JUNIT_OTR_SOCKET_PORT} -Djunit.platform.output.capture.stdout=true -Djunit.platform.output.capture.stderr=true"
fi

trap 'docker-compose kill && docker-compose down; [[ -z ${otr_relay} ]] || docker rm -f "${otr_relay}" >/dev/null' EXIT

docker-compose run -e "MAVEN_ARGS=${MAVEN_ARGS}" --name mvn -T --rm -v "${MAVEN_SETTINGS}:${MAVEN_SETTINGS}:Z" mvn bash -s <<-INSIDE
	set-java.sh ${jdk}

	# Ensure that Jenkins node setup does not influence the container Java setup
	unset JAVA_HOME

	java -version
	mvn -v

	if [[ -n "${JUNIT_OTR_SOCKET_PORT:-}" ]]; then
		socat TCP-LISTEN:${JUNIT_OTR_SOCKET_PORT:-},bind=127.0.0.1,fork,reuseaddr TCP:host.docker.internal:${JUNIT_OTR_SOCKET_PORT:-} &
	fi

	run.sh remote-webdriver-${browser} ${jenkinsVersion} ${otr_args} -Dmaven.test.failure.ignore=true -Dcsp.rule -DforkCount=1 -B
INSIDE
status=$?

if [[ -d target/surefire-reports ]]; then
	find target/surefire-reports -type f -name 'TEST-*.xml' -print0 |
		xargs -0 sed -i 's!\[\[ATTACHMENT|/home/ath-user/sources/target\(/[^]]*\)\]\]![[ATTACHMENT|'"$PWD"'/target\1]]!g'
else
	echo 'No test results to be saved'
fi

exit $status
