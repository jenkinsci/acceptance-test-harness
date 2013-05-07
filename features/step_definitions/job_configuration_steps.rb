
When /^I configure the job$/ do
  @job.configure
end

When /^I add a shell build step "([^"]*)"$/ do |script|
  @job.add_shell_step(script)
end

When /^I add a shell build step$/ do |script|
  @job.add_shell_step(script)
end

When /^I add a shell build step "([^"]*)" in the job configuration$/ do |script|
  @job.configure do
    @job.add_shell_step(script)
  end
end

When /^I add a shell build step in the job configuration$/ do |script|
  @job.configure do
    @job.add_shell_step(script)
  end
end

When /^I change a shell build step to "([^"]*)"$/ do |script|
  @job.change_script_step(script)
end


When /^I add always fail build step$/ do
  @job.add_shell_step "exit 1"
end

When /^I add always fail build step in the job configuration$/ do
  @job.configure do
    step 'I add always fail build step'
  end
end

When /^I add "([^"]*)" build action$/ do |action|
  @job.add_build_action(action)
end

When /^I add "([^"]*)" post-build action$/ do |action|
  @job.add_postbuild_action(action)
end

When /^I tie the job to the "([^"]*)" label$/ do |label|
  @job.configure do
    @job.label_expression = label
  end
end

When /^I tie the job to the slave$/ do
  step %{I tie the job to the "#{@slave.name}" label}
end

When /^I enable concurrent builds$/ do
  step %{I check the "_.concurrentBuild" checkbox}
end

When /^I add a string parameter "(.*?)"$/ do |string_param|
  @job.configure do
    @job.add_parameter("String Parameter",string_param,string_param)
  end
end

When /^I disable the job$/ do
  @job.configure do
    @job.disable
  end
end

When /^I set artifacts? "([^"]*)" to archive$/ do |artifacts|
  @job.archive_artifacts includes:artifacts
end

When /^I set artifacts? "([^"]*)" to archive in the job configuration$/ do |artifacts|
  @job.configure do
    @job.archive_artifacts includes:artifacts
  end
end

When /^I set artifacts? "([^"]*)" to archive and exclude "([^"]*)" in the job configuration$/ do |include, exclude|
  @job.configure do
    @job.archive_artifacts(includes: include, excludes: exclude)
  end
end

When /^I want to keep only the latest successful artifacts$/ do
  @job.configure do
    @job.archive_artifacts(latestOnly: true)
  end
end

When /^I set (\d+) builds? to keep$/ do |number|
  step %{I check the "logrotate" checkbox}

  name = if @runner.jenkins_version < Gem::Version.new('1.503') then
    'logrotate_nums' else '_.numToKeepStr'
  end

  find(:xpath, "//input[@name='#{name}']").set(number)
end

When /^I schedule job to run periodically at "([^"]*)"$/ do |schedule|
  step 'I check the "hudson-triggers-TimerTrigger" checkbox'
  find(:path, '/hudson-triggers-TimerTrigger/spec').set(schedule)
end

When /^I use "([^"]*)" as custom workspace$/ do |workspace|
  @job.use_custom_workspace(workspace)
end

When /^I copy resource "([^"]*)" into workspace as "([^"]*)" via shell command$/ do |resource, target|
  @job.add_shell_step("cp #{File.dirname(__FILE__)}/../../resources/#{resource} ./#{target}")
end


Then /^the job should be able to use the "(.*)" buildstep$/ do |build_step|
  find(:xpath, "//button[text()='Add build step']").click
  find(:xpath, "//a[text()='#{build_step}']").instance_of?(Capybara::Node::Element).should be true
end

Then /^I should be able to use the "([^"]*)" build environment action$/ do |env_action|
  find(:xpath, "//label[text()='#{env_action}']").instance_of?(Capybara::Node::Element).should be true
end

Then /^I should be able to use "([^"]*)" post-build action$/ do |action|
  find(:xpath, "//button[text()='Add post-build action']").click
  find(:xpath, "//a[text()='#{action}']").instance_of?(Capybara::Node::Element).should be true
end
