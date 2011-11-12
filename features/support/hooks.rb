#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require File.dirname(__FILE__) + "/../../lib/runner.rb"

Before do
  @runner = Jenkins::Runner.new
  @runner.setup
  @base_url = @runner.base_url
  Capybara.app_host = @base_url
end

After do |scenario|
  @runner.teardown scenario.failed?
end
