#!/bin/bash

GECKO_DRIVER_PATH=/usr/local/bin/geckodriver
CHROME_DRIVER_PATH=/usr/local/bin/chromedriver

BROWSER=chrome
RETRY=2
TEST_CASE=$1
ELASTIC=5 # increase if your machine is slow

if [ -z "$TEST_CASE" ];
then
    TEST_CASE=WarningsNextGenerationPluginTest
fi

mvnOptions="-Dsurefire.rerunFailingTestsCount=${RETRY} -Dwebdriver.gecko.driver=${GECKO_DRIVER_PATH} -DElasticTime.factor=${ELASTIC} -Dwebdriver.chrome.driver=${CHROME_DRIVER_PATH}"

echo Running: env LC_NUMERIC="en_US.UTF-8" BROWSER=${BROWSER} mvn test -o -Dtest=${TEST_CASE} ${mvnOptions}

env LC_NUMERIC="en_US.UTF-8" BROWSER=${BROWSER} mvn test -o -Dtest=${TEST_CASE} ${mvnOptions}
