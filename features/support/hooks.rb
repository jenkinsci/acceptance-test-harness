#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

$LOAD_PATH.push File.dirname(__FILE__) + "/../.."
Dir.glob(File.dirname(__FILE__) + "/../../lib/controller/*.rb") do |name|
  require name
end

controller_factory = DefaultJenkinsControllerFactory.new()
if ENV['PRELAUNCH']
  controller_factory = CachedJenkinsControllerFactory.new(controller_factory)
end

Before do |scenario|

  # skip scenario and initialization when not applicable
  next if scenario.skip_not_applicable($version)

  # in case we are using Sauce, set the test name
  Sauce.config do |c|
    c[:name] = Sauce::Capybara::Cucumber.name_from_scenario(scenario)
  end

  @runner = controller_factory.create(@controller_options||{})
  @runner.start
  $version = @runner.jenkins_version
  at_exit do
    @runner.stop
    @runner.teardown
  end
  @base_url = @runner.url
  Capybara.app_host = @base_url

  scenario.skip_not_applicable($version)
end

After do |scenario|
  next if @runner.nil? # skip if not initialized
  @runner.diagnose if scenario.failed?
  @runner.stop # if test fails, stop in at_exit is not called
  @runner.teardown
end
