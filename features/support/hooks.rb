#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

$LOAD_PATH.push File.dirname(__FILE__) + "/../.."
require "lib/jenkins_controller.rb"
require "lib/local_controller.rb"
require "lib/sysv_init_controller.rb"

Before do

  # default is to run locally, but allow the parameters to be given as env vars
  # so that rake can be invoked like "rake test type=remote_sysv"
  if ENV['type']
    controller_args = {}
    ENV.each { |k,v| controller_args[k.to_sym]=v }
  else
    controller_args = { :type => :local }
  end

  @runner = JenkinsController.create(controller_args)
  @runner.start
  at_exit do
    @runner.stop
  end
  @base_url = @runner.url
  Capybara.app_host = @base_url
end

After do |scenario|
end
