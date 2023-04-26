#!/usr/bin/env bash
set -euo pipefail

#
# Extract the core commit from the WAR.
#
# TODO handle transitive dependencies like Stapler, Remoting, etc.
#

war=
if [[ -f target/jenkins-war.war ]]; then
	war=target/jenkins-war.war
elif [[ -f jenkins.war ]]; then
	war=jenkins.war
else
	echo 'Failed to locate WAR' >&2
	exit 1
fi

jar xf "${war}" META-INF/MANIFEST.MF
core_commit=$(awk '/Implementation-Build:/ {print $2}' META-INF/MANIFEST.MF)
rm -f META-INF/MANIFEST.MF
rm -df META-INF
echo "${core_commit}"
