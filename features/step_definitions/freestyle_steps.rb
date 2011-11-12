#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2


Given /^a Jenkins instance$/ do
end

When /^I create a job named "([^"]*)"$/ do |name|
  @job = Job.create_freestyle(@base_url, name)
end

When /^I add a script build step to run "([^"]*)"$/ do |script|
  @job.configure do
    @job.add_script_step(script)
  end
end

When /^I run the job$/ do
  @job.queue_build
end

Then /^I should see console output matching "([^"]*)"$/ do |script|
  @job.last_build.console.should match /#{Regexp.escape(script)}/
end


