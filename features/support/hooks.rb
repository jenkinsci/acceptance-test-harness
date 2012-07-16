#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

$LOAD_PATH.push File.dirname(__FILE__) + "/../.."
require "lib/controller/jenkins_controller.rb"
require "lib/controller/local_controller.rb"
require "lib/controller/sysv_init_controller.rb"

Before do |scenario|

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
    @runner.stop if @runner.is_running? 
    @runner.teardown
  end
  @base_url = @runner.url
  Capybara.app_host = @base_url

  # wait for Jenkins to properly boot up and finish initialization
  s = Capybara.current_session
  for i in 1..20 do
    begin
      s.visit "/systemInfo"
      s.find "TABLE.bigtable"
      break # found it
    rescue => e
      sleep 0.5
    end
  end
end

After do |scenario|
  @runner.stop if @runner.is_running? # if test fails, stop in at_exit is not called
  STDOUT.puts @runner
end
