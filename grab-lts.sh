#!/bin/bash

if [[ $# -eq 0 ]] ; then
    echo "$0 jenkins-version"
    exit 0
fi


curl -f -L "http://mirrors.jenkins-ci.org/war-stable/$1/jenkins.war" > jenkins/jenkins-$1.war
