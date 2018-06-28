#!/bin/sh

GECKO_DRIVER_PATH="U:\Anna\chromedriver\chromedriver.exe"
BROWSER=chrome
RETRY=0
TEST_CASE=\#$1
ELASTIC=1 # increase if your machine is slow

if [[ ${TEST_CASE} = '#' ]] ; then
    TEST_CASE=
fi

mvnOptions="-Dsurefire.rerunFailingTestsCount=${RETRY} -Dwebdriver.chrome.driver=${GECKO_DRIVER_PATH} -DElasticTime.factor=${ELASTIC}"

echo env LC_NUMERIC=”en_US.UTF-8? BROWSER=${BROWSER} mvn -q test -o -Dtest=WarningsPluginTest${TEST_CASE} ${mvnOptions}
env LC_NUMERIC=”en_US.UTF-8” BROWSER=${BROWSER} JENKINS_WAR=./jenkins/jenkins-2.107.3.war TYPE=winstone PLUGINS_DIR=../jenkins/plugins BROWSER=chrome WORKSPACE=/tmp mvn -q test -o -Dtest=AnalysisPluginsTest${TEST_CASE} ${mvnOptions}
