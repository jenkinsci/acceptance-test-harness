#!/bin/sh

die() {
	echo "$(basename "$0"): $*" >&2
	exit 1
}

#
# Script for setting all the variables to run the ATH locally on Unix
#

export DISPLAY=:0
export INTERACTIVE=false
export BROWSER=remote-webdriver-firefox
export REMOTE_WEBDRIVER_URL=http://127.0.0.1:4444/wd/hub
export JENKINS_JAVA_OPTS=-Xmx1280m
if [ -z "${JENKINS_WAR}" ] && [ -f /usr/share/java/jenkins.war ]; then
	export JENKINS_WAR=/usr/share/java/jenkins.war
fi

IP=$(ip addr show docker0 2>/dev/null | grep 'inet ' | awk '{print $2}' | cut -d'/' -f1) ||
	die "failed to retrieve IP address of Docker interface"
export SELENIUM_PROXY_HOSTNAME="${IP}"
export TESTCONTAINERS_HOST_OVERRIDE="${IP}"
export JENKINS_LOCAL_HOSTNAME="${IP}"

echo "To start the remote Firefox container, run the following command:"
echo "docker run --shm-size=2g -d -p 127.0.0.1:4444:4444 -p 127.0.0.1:5900:5900 -e no_proxy=localhost -e SE_SCREEN_WIDTH=1680 -e SE_SCREEN_HEIGHT=1090 selenium/standalone-firefox:4.40.0"
