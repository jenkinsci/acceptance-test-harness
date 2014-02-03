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

Then /^the build should run on the slave$/ do
  @job.last_build.wait_until_finished.node.should eq @slave.name
end

Then /^the build should run on "(.*?)"$/ do |name|
  @job.last_build.wait_until_finished.node.should eq name
end

Then /^the build #(\d+) should run on "(.*?)"$/ do |number, name|
  @job.build(number).wait_until_finished.node.should eq name
end

Then /^I should see "([^"]*)" executors configured$/ do |count|
  visit("/computer/#{@slave.name}")
  @slave.executor_count.should == count.to_i
end

Then /^jobs should be executed in order on the slave$/ do |table|
  visit @slave.url + '/builds'
  page.text.should match table.raw[0].reverse.join('.*')
end
