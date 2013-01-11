
When /^I configure the job$/ do
  @job.configure
end

When /^I add a script build step to run "([^"]*)"$/ do |script|
  @job.add_script_step(script)
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
  step %{I click the "_.concurrentBuild" checkbox}
end

When /^I add a string parameter "(.*?)"$/ do |string_param|
  @job.configure do
    @job.add_parameter("String Parameter",string_param,string_param)
  end
end
When /^I add an Ant build step for:$/ do |ant_xml|
  @job.configure do
    @job.add_script_step("cat > build.xml <<EOF
  #{ant_xml}
EOF")
    @job.add_ant_step('hello', 'build.xml')
  end
end

When /^I disable the job$/ do
  @job.configure do
    @job.disable
  end
end

Then /^the job should be able to use the "(.*)" buildstep$/ do |build_step|
  find(:xpath, "//button[text()='Add build step']").click
  find(:xpath, "//a[text()='#{build_step}']").instance_of?(Capybara::Node::Element).should be true
end
