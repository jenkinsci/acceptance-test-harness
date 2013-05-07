When /^I configure user axis "([^"]*)" with values "([^"]*)"$/ do |name, value|
  @job.add_user_axis(name, value)
end

When /^I configure label expression axis with values "([^"]*)"$/ do |value|
  @job.add_label_expression_axis(value)
end

When /^I configure slaves axis with value "([^"]*)"$/ do |value|
  @job.add_slaves_axis(value)
end

When /^I configure JDK axis with values "([^"]*)"$/ do |value|
  @job.add_jdk_axis(value)
end

When /^I configure combination filter with values "([^"]*)"$/ do |filter|
  @job.combination_filter(filter)
end

When /^I configure to run configurations sequentially$/ do
  @job.run_configurations_sequentially
end

When /^I configure to execute touchstone builds first with filter "([^"]*)" and required result "([^"]*)"$/ do |filter, result|
  @job.touchstone_builds_first(filter, result)
end
