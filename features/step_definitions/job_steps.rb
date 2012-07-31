#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2


Given /^a simple job$/ do
  @job = Jenkins::Job.create_freestyle(@base_url, Jenkins::Job.random_name)
  @job.configure do
    @job.add_script_step("ls")
  end
end

Given /^a job$/ do
  @job = Jenkins::Job.create_freestyle(@base_url, Jenkins::Job.random_name)
end


When /^I create a job named "([^"]*)"$/ do |name|
  @job = Jenkins::Job.create_freestyle(@base_url, name)
end

When /^I run the job$/ do
  @job.queue_build
end

When /^I save the job$/ do
  @job.save
end

When /^I visit the job page$/ do
  @job.open
end

When /^I build (\d+) jobs$/  do |count|
  count.to_i.times do |i|
    @job.queue_build
  end
  sleep 6 # Hard-coded sleep to allow the queue delay in Jenkins to expire
end

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

Then /^the build should succeed$/ do
  @job.last_build.succeeded?.should be true
end

Then /^it should be disabled$/ do
  page.should_not have_content 'Build Now'
end

Then /^it should have an "(.*?)" button on the job page$/ do |button|
  @job.open
  page.should have_xpath("//button[text()='Enable']")
end

