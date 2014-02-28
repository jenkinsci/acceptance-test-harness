# This is not loaded by cucumber in dryrun mode
require File.dirname(__FILE__) + '/env.rb'

controller_factory = JenkinsControllerFactory.get

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
  next if scenario.skip_not_applicable

  @runner = controller_factory.create(@controller_options||{})
  @runner.start
  $jenkins = Jenkins::JenkinsRoot.new @runner

  @cleanup << Proc.new do
    @runner.stop # if test fails, stop in at_exit is not called
    @runner.teardown
    @runner = nil
  end

  @base_url = @runner.url
  Capybara.app_host = @base_url

  scenario.skip_not_applicable @runner
end

After do |scenario|
  @cleanup.each { |c| c.call() }
  @cleanup.clear
end

After("~@nojenkins") do |scenario|
  @runner.diagnose if @runner && scenario.failed?
end
