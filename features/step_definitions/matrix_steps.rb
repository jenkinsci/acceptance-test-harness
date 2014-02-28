Given /^a matrix job$/ do
  @job = Jenkins::Job.create('Matrix', @base_url)
end

When /^I create a matrix job$/ do
  @job = Jenkins::Job.create('Matrix', @base_url)
end

When /^I configure user axis "([^"]*)" with values "([^"]*)"$/ do |name ,value|
  @job.add_user_axis(name, value)
end

When /^I configure slaves axis with value "([^"]*)"$/ do |value|
  @job.add_slaves_axis(value)
end

When /^I configure to run configurations sequentially$/ do
  @job.run_configurations_sequentially
end

When /^I configure to execute touchstone builds first with filter "([^"]*)" and required result "([^"]*)"$/ do |filter, result|
  @job.touchstone_builds_first(filter, result)
end

When /^I set combination filter to "([^"]*)"$/ do |filter|
  find(:path, '/hasCombinationFilter').check
  find(:path, '/hasCombinationFilter/combinationFilter').set(filter)
end

Then /^combination "([^"]*)" (should|should not) be built$/ do |configuration, should_or_not|
  config = @job.last_build.wait_until_finished.configuration(configuration)
  config.exists?.send should_or_not, be
end

Then /^combination "([^"]*)" (should|should not) be built in build (\d+)$/ do |configuration, should_or_not, build|
  config = @job.build(build).wait_until_finished.configuration(configuration)
  config.exists?.send should_or_not, be
end

Then /^the configuration "([^"]*)" should be built on "([^"]*)"$/ do |configuration, slave|
  config = @job.configuration configuration
  expression = "(Building|Building remotely)( on " + slave +")"
  config.last_build.wait_until_finished.console.should match expression
end

Then /^I console output of configurations should match "([^"]*)"$/ do |script|
  @job.last_build.wait_until_finished
  configurations = @job.configurations
  index = 0
  while index<configurations.length do
    configurations[index].last_build.console.should match /#{Regexp.escape(script)}/
    index += 1
  end
end

Then /^the configurations should run sequentially$/ do
  build = @job.last_build.wait_until_started
  configurations = build.configurations
  while build.in_progress? do

    running = 0
    configurations.each do |config|
      running += 1 if config.in_progress?
    end

    running.should be < 2, "#{running} configurations are running at the same time"
    sleep 0.5
  end
end
