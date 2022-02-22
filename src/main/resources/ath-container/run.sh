#!/bin/bash


if [ $# -lt 2 ]; then
	cat <<USAGE
Usage: $0 BROWSER JENKINS [ARGS]

It can use jenkins.war from local maven repository or download it when missing.

BROWSER: Value for BROWSER variable
JENKINS: Path to the jenkins.war, Jenkins version of one of "latest", "latest-rc", "lts" and "lts-rc"

Examples:

# Run full suite in FF against ./jenkins.war.
$ ./run firefox ./jenkins.war

# Run Ant plugin test in chrome against Jenkins 1.512.
$ ./run chrome 1.512 -Dtest=AntPluginTest

# Run full suite in FF against LTS release candidate
$ ./run firefox lts-rc
USAGE
  exit 2
fi

function download() {
    echo "Fetching $1 to $2"
    status=$(curl -sSL --write-out "%{http_code}" --retry 3 --retry-delay 0 --retry-max-time 60 -o $2 $1)
    if [ "$status" -ne 200 ]; then
        echo >&2 "Failed to fetch the $1 ($status) to $2"
        return 1
    fi
}

if [ -z "$DISPLAY" ] && [ -z "$BROWSER_DISPLAY" ]; then
    echo >&2 "Neither DISPLAY nor BROWSER_DISPLAY defined. Is the VNC server running?"
    exit 1
fi

browser=$1
war=$2
if [ ! -f $war ]; then
    mirrors=http://mirrors.jenkins-ci.org
    case "$war" in
        "latest")
            war=jenkins-latest.war
            url=https://ci.jenkins.io/job/Core/job/jenkins/job/PR-6295/3/artifact/org/jenkins-ci/main/jenkins-war/2.336-rc32128.b_a_defd67dd21/jenkins-war-2.336-rc32128.b_a_defd67dd21.war
        ;;
        "latest-rc")
            war=jenkins-latest-rc.war
            url=$mirrors/war-rc/latest/jenkins.war
        ;;
        "lts")
            war=jenkins-lts.war
            url=$mirrors/war-stable/latest/jenkins.war
        ;;
        "lts-rc")
            war=jenkins-lts-rc.war
            url=$mirrors/war-stable-rc/latest/jenkins.war
        ;;
    esac

    if [ -n "$url" ]; then
        find $war -maxdepth 0 -mtime +1 -delete 2> /dev/null
        if [ ! -f $war ]; then
            download "$url" "$war" || exit 1
        fi
    fi
fi

if [ ! -f $war ] && [[ $war == *.war ]]; then
    download "$war" "jenkins.war" || exit 1
    war=jenkins.war
fi

if [ ! -f $war ]; then

	wardir=~/.m2/repository/org/jenkins-ci/main/jenkins-war

    war=$wardir/$2/jenkins-war-$2.war
    if [ ! -f $war ]; then

        mvn -B org.apache.maven.plugins:maven-dependency-plugin:2.7:get\
            -DremoteRepositories=repo.jenkins-ci.org::::https://repo.jenkins-ci.org/public/\
            -Dartifact=org.jenkins-ci.main:jenkins-war:$2:war
    fi

    if [ ! -f $war ]; then

        echo "No such jenkins.war. Available local versions:"
        ls $wardir/*/jenkins-war-*.war | sed -r -e 's/.*jenkins-war-(.+)\.war/\1/'
        exit 1
    fi
fi

shift 2

set -x

BROWSER=$browser JENKINS_WAR=$war mvn --show-version --no-transfer-progress test "$@"
