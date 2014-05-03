#!/bin/bash
# Build step to run tests

curl -s -o use-ruby https://repository-cloudbees.forge.cloudbees.com/distributions/ci-addons/ruby/use-ruby
RUBY_VERSION=2.0.0-p247 source ./use-ruby

gem install --conservative bundle
bundle update

curl -s -o jenkins.war $WAR_URL

STARTUP_TIMEOUT=240 JENKINS_WAR=jenkins.war bundle exec rake
