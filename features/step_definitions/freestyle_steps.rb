#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2


Given /^a job$/ do
  @job = Jenkins::Job.create_freestyle(@base_url, Jenkins::Job.random_name)
end


############################################################################


When /^I configure the job$/ do
  @job.configure
end

When /^I visit the home page$/ do
  visit "/"
end

When /^I create a job named "([^"]*)"$/ do |name|
  @job = Jenkins::Job.create_freestyle(@base_url, name)
end

When /^I add a script build step to run "([^"]*)"$/ do |script|
  @job.add_script_step(script)
end

When /^I run the job$/ do
  @job.queue_build
end

When /^I click the "([^"]*)" checkbox$/ do |name|
  find(:xpath, "//input[@name='#{name}']").set(true)
end

When /^I enable concurrent builds$/ do
  step %{I click the "_.concurrentBuild" checkbox}
end

When /^I save the job$/ do
  @job.save
end

When /^I visit the job page$/ do
  @job.open
end

When /^I tie the job to the "([^"]*)" label$/ do |label|
  @job.configure do
    @job.label_expression = label
  end
end

When /^I tie the job to the slave$/ do
  step %{I tie the job to the "#{@slave.name}" label}
end

When /^I build (\d+) jobs$/  do |count|
  count.to_i.times do |i|
    @job.queue_build
  end
  sleep 6 # Hard-coded sleep to allow the queue delay in Jenkins to expire
end

When /^I add a string parameter "(.*?)"$/ do |string_param|
  @job.configure do
    @job.add_parameter("String Parameter",string_param,string_param)
  end
end


############################################################################


Then /^I should see console output matching "([^"]*)"$/ do |script|
  @job.last_build.console.should match /#{Regexp.escape(script)}/
end


Then /^the (\d+) jobs should run concurrently$/ do |count|
  count.to_i.times do |i|
    # Build numbers start at 1
    @job.build(i + 1).in_progress?.should be true
  end
end

Then /^I should be prompted to enter the "(.*?)" parameter$/ do |param_name|
  find(:xpath, "//input[@value='#{param_name}']").instance_of?(Capybara::Node::Element).should be true
end

When /^I add an Ant build step for:$/ do |ant_xml|
  @job.configure do
    @job.add_script_step("cat > build.xml <<EOF
  #{ant_xml}
EOF")
    @job.add_ant_step('hello', 'build.xml')
  end
end

Then /^the build should succeed$/ do
  @job.last_build.succeeded?.should be true
end

When /^I disable the job$/ do
  @job.configure do
    @job.disable
  end
end

Then /^it should be disabled$/ do
  page.should_not have_content 'Build Now'
end

Then /^it should have an "(.*?)" button on the job page$/ do |button|
  @job.open
  page.should have_xpath("//button[text()='Enable']")
end

