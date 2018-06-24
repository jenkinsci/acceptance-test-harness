#!/bin/bash

GECKO_DRIVER_PATH=/usr/local/bin/geckodriver
BROWSER=chrome
RETRY=1
TEST_CASE=\#$1
ELASTIC=5 # increase if your machine is slow

if [[ ${TEST_CASE} = '#' ]] ; then
    TEST_CASE=
fi

mvnOptions="-Dsurefire.rerunFailingTestsCount=${RETRY} -Dwebdriver.gecko.driver=${GECKO_DRIVER_PATH} -DElasticTime.factor=${ELASTIC} -Dwebdriver.chrome.driver=/usr/lib/chromium-browser/chromedriver"

echo env LC_NUMERIC=”en_US.UTF-8″ BROWSER=${BROWSER} mvn -q test -o -Dtest=WarningsPluginTest${TEST_CASE} ${mvnOptions}
env LC_NUMERIC=”en_US.UTF-8″ BROWSER=${BROWSER} mvn -q test -o -Dtest=WarningsPluginTest${TEST_CASE} ${mvnOptions}
