#!/usr/bin/env bash
# Inspired by https://disconnected.systems/blog/another-bash-strict-mode/
set -uo pipefail
trap 's=$?; echo >&2 "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $s' ERR

# The selection used by update-alternatives for each java version
if [[ $1 == '17' ]]; then
	selection='17-openjdk'
elif [[ $1 == '21' ]]; then
	selection='21-openjdk'
elif [[ $1 == '25' ]]; then
	selection='25-openjdk'
else
	echo >&2 "Unsupported java version '${1}'"
	exit 1
fi

# For some reason, all tools from JDK are split to 2 groups named java and javac

JAVA_PATH=$(update-alternatives --list java | grep $selection)
JAVAC_PATH=$(update-alternatives --list javac | grep $selection)

update-alternatives --set java "$JAVA_PATH"
update-alternatives --set javac "$JAVAC_PATH"

echo
echo -------------------- INFORMATION --------------------
echo Running on...
java -version
echo
javac -version
echo
echo Start running tests with...
echo
echo run.sh remote-webdriver-firefox latest -DforkCount=1 -Dmaven.test.failure.ignore=true -B -Dtest=...
echo
echo ------------------ END INFORMATION ------------------
echo
