#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

Given /^a dumb slave$/ do
  @slave = Jenkins::Slave.dumb_slave(@base_url, Jenkins::Slave.random_name)
end


############################################################################


When /^I create dumb slave named "([^"]*)"$/ do |name|
  @slave = Jenkins::Slave.dumb_slave(@base_url, name)
end


############################################################################


When /^I add the label "([^"]*)" to the slave$/ do |label|
  @slave.configure do
    @slave.labels = label
  end
end

When /^I set the executors to "([^"]*)"$/ do |count|
  @slave.configure do
    @slave.executors = count
  end
end


############################################################################


Then /^I should see the job tied to the "([^"]*)" label$/ do |label|
  visit("/label/#{label}")
  step %{the page should say "#{@job.name}"}
end

Then /^I should see the job tied to the slave$/ do
  step %{I should see the job tied to the "#{@slave.name}" label}
end

Then /^I should see "([^"]*)" executors configured$/ do |count|
  visit("/computer/#{@slave.name}")
  @slave.executor_count.should == count.to_i
end
