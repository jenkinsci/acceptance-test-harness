#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

$LOAD_PATH.push File.dirname(__FILE__) + "/../../lib"

require 'capybara/cucumber'
require 'sauce/cucumber'
require 'jenkins/cucumber'
require 'jenkins/env'
