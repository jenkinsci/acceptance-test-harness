#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

$LOAD_PATH.push File.dirname(__FILE__) + "/../.."
Dir.glob(File.dirname(__FILE__) + "/../../lib/jenkins/controller/*.rb") do |name|
  require name
end

controller_factory = DefaultJenkinsControllerFactory.new()
if ENV['PRELAUNCH']
  controller_factory = CachedJenkinsControllerFactory.new(controller_factory)
end

Before do |scenario|
  # in case we are using Sauce, set the test name
  Sauce.config do |c|
    c[:name] = Sauce::Capybara::Cucumber.name_from_scenario(scenario)
  end

  @cleanup = [] # procs/lambdas that are run for cleanup
  at_exit do  # in case VM got aborted before it gets to 'After'
    @cleanup.each { |c| c.call() }
  end
end

Before("~@nojenkins") do |scenario|
  # skip scenario and initialization when not applicable
  next if scenario.skip_not_applicable($version)

  @runner = controller_factory.create(@controller_options||{})
  @runner.start
  $version = @runner.jenkins_version

  @cleanup << Proc.new do
    @runner.stop # if test fails, stop in at_exit is not called
    @runner.teardown
    @runner = nil
  end

  @base_url = @runner.url
  Capybara.app_host = @base_url

  scenario.skip_not_applicable($version)
end

After("~@nojenkins") do |scenario|
  @runner.diagnose if scenario.failed?
end

After do |scenario|
  @cleanup.each { |c| c.call() }
  @cleanup.clear
end
