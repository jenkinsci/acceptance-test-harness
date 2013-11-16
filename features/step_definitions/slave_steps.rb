Given /^a dumb slave$/ do
  @slave = Jenkins::Slave.named_slave(@base_url, Jenkins::Slave.random_name)
end

Given /^a slave named "([^"]*)"$/ do |name|
  @slave = Jenkins::Slave.named_slave(@base_url, name)
end


When /^I create dumb slave named "([^"]*)"$/ do |name|
  @slave = Jenkins::Slave.named_slave(@base_url, name)
end

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


Then /^the job should be tied to the "([^"]*)" label$/ do |label|
  visit("/label/#{label}")
  step %{the page should say "#{@job.name}"}
end

Then /^the job should be tied to the slave$/ do
  step %{the job should be tied to the "#{@slave.name}" label}
end

Then /^the build should run on the slave$/ do |slave|
  @job.last_build.console.should include "Building remotely on #{@slave.name}"
end

Then /^I should see "([^"]*)" executors configured$/ do |count|
  visit("/computer/#{@slave.name}")
  @slave.executor_count.should == count.to_i
end
