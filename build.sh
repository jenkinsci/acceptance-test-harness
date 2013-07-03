#!/bin/bash
# Build step to run tests

curl -s -o use-ruby https://repository-cloudbees.forge.cloudbees.com/distributions/ci-addons/ruby/use-ruby
RUBY_VERSION=1.9.3-p194 source ./use-ruby

gem install --conservative bundle
bundle update

curl -s -o jenkins.war https://ci.jenkins-ci.org/job/jenkins_main_trunk/lastSuccessfulBuild/artifact/war/target/jenkins.war

JENKINS_WAR=jenkins.war bundle exec rake
