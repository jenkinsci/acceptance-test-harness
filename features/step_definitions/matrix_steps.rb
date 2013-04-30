When /^I configure user axis "([^"]*)" with values "([^"]*)"$/ do |name, value|
  if !(page.current_url.eql? @job.configure_url)
      visit @job.configure_url
  end
  find(:xpath, "//button[text()='Add axis']").click
  find(:xpath, "//li/a[text()='User-defined Axis']").click
  sleep 0.1 # wait until axis appear
  input = "//div[@name='axis' and @descriptorid='hudson.matrix.TextAxis']//td/input";
  find(:xpath, "(#{input}[@name='_.name'])[last()]").set(name)
  find(:xpath, "(#{input}[@name='_.valueString'])[last()]").set(value)
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
