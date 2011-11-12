#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2


Given /^a bare Jenkins instance$/ do
end

Given /^a job$/ do
  suffix = (rand() * 1_000_000).to_s[0 .. 20]
  name = "test_job_#{suffix}"
  @job = Job.create_freestyle(@base_url, name)
end


############################################################################


When /^I configure the job$/ do
  @job.configure
end

When /^I create a job named "([^"]*)"$/ do |name|
  @job = Job.create_freestyle(@base_url, name)
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

When /^I save the job$/ do
  @job.save
end


############################################################################


Then /^I should see console output matching "([^"]*)"$/ do |script|
  @job.last_build.console.should match /#{Regexp.escape(script)}/
end

Then /^the job page should say "([^"]*)"$/ do |content|
  @job.open
  page.should have_content(content)
end
