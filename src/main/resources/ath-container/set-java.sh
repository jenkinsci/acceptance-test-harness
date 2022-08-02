#!/usr/bin/env bash
# Inspired by https://disconnected.systems/blog/another-bash-strict-mode/
set -uo pipefail
trap 's=$?; echo >&2 "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $s' ERR

# The selection used by update-alternatives for each java version
if [ "$1" == "11" ]; then
    selection="openjdk-11-jdk.$(arch)"
elif [ "$1" == "8" ]; then
    selection="openjdk-8-jdk.$(arch)"
else
    echo >&2 "Unsupported java version '${1}'"
    exit 1
fi

# For some reason, all tools from JDK are split to 2 groups named java and javac
update-alternatives --set java  "$selection"
update-alternatives --set javac "$selection"

echo
echo -------------------- INFORMATION --------------------
echo Running on...
java -version
echo
javac -version
echo
echo Start running tests with...
echo
echo 'eval $(vnc.sh)'
echo
echo run.sh firefox latest -DforkCount=1 -Dmaven.test.failure.ignore=true -B -Dtest=...
echo
echo ------------------ END INFORMATION ------------------
echo
