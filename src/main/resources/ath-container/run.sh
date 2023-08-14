#!/bin/bash

if [[ $# -lt 2 ]]; then
	cat <<-USAGE
		Usage: $0 BROWSER JENKINS [ARGS]

		It can use jenkins.war from local maven repository or download it when missing.

		BROWSER: Value for BROWSER variable
		JENKINS: Path to the Jenkins WAR, URL to the Jenkins WAR, version of the Jenkins WAR, "latest", or "lts"

		Examples:

		# Run full suite in Firefox against a URL:
		$ ./run firefox https://updates.jenkins.io/download/war/2.394/jenkins.war

		# Run full suite in Firefox against ./jenkins.war:
		$ ./run firefox ./jenkins.war

		# Run Ant plugin test in chrome against Jenkins 2.399:
		$ ./run chrome 2.399 -Dtest=AntPluginTest

		# Run full suite in Firefox against LTS:
		$ ./run firefox lts
	USAGE
	exit 2
fi

MVN='mvn -V -e -ntp'
if [[ -n ${MAVEN_SETTINGS-} ]]; then
	MVN="${MVN} -s ${MAVEN_SETTINGS}"
fi

function download() {
	echo "Fetching $1 to $2"
	status=$(curl --http1.1 -sSL --write-out '%{http_code}' --retry 3 --retry-delay 0 --retry-max-time 60 -o "$2" "$1")
	if [[ $status -ne 200 ]]; then
		echo >&2 "Failed to fetch the $1 ($status) to $2"
		return 1
	fi
}

if [[ -z $DISPLAY ]] && [[ -z $BROWSER_DISPLAY ]]; then
	echo >&2 'Neither DISPLAY nor BROWSER_DISPLAY defined. Is the VNC server running?'
	exit 1
fi

browser=$1
war=$2
extra_args=

if [[ ! -f $war ]]; then
	case "$war" in
	latest)
		# Maven will download this in the process-test-resources phase
		war=target/jenkins-war.war
		;;
	lts)
		# Maven will download this in the process-test-resources phase
		war=target/jenkins-war.war
		extra_args='-Plts'
		;;
	*.war)
		download "$war" "jenkins.war" || exit 1
		war=jenkins.war
		;;
	*)
		# Maven will download this in the process-test-resources phase
		war=target/jenkins-war.war
		extra_args="-Djenkins.version=$2"
		;;
	esac
fi

shift 2

if [[ $# -eq 0 ]]; then
	extra_args+=' test'
fi

set -x

BROWSER=$browser JENKINS_LOCAL_HOSTNAME=jenkins.127.0.0.1.sslip.io JENKINS_WAR=$war $MVN $extra_args "$@"
