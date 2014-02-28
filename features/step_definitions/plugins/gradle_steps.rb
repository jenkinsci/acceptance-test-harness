Given /^I have Gradle "([^"]*)" auto-installation named "([^"]*)" configured$/ do |version, name|
  @runner.wait_for_updates 'Gradle'
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Gradle")
    sleep 1
    find(:xpath, "//div[@name='tool' and child::div[text()='Gradle']][last()]//input[@name='_.name']").set(name)
    find(:xpath, "//div[@name='tool' and child::div[text()='Gradle']][last()]//input[@name='hudson-tools-InstallSourceProperty']").set(true)
    find(:xpath, "//div[@name='tool' and child::div[text()='Gradle']][last()]//select[@name='_.id']/option[@value='#{version}']").click
  end
end

When /^I add script for creating "([^"]*)" file :$/ do |file, gradle_script|
  @job.add_shell_step("cat > #{file} << EOF \n #{gradle_script} \nEOF")
end

When /^I add script for creating "([^"]*)" file in directory "([^"]*)" :$/ do |file, directory, gradle_script|
  @job.add_shell_step("mkdir #{directory} \n cd #{directory} \n cat > #{file} << EOF \n #{gradle_script} \nEOF")
end

When /I add Gradle build step/ do
  @step = @job.add_build_step 'Gradle'
end

When /^I set Gradle script file name "([^"]*)"$/ do |name|
  @step.file name
end

When /^I set Gradle script direcotry path "([^"]*)"$/ do |path|
  @step.dir path
end

When /^I set Gradle switches "([^"]*)"$/ do |switches|
  @step.switches switches
end

When /^I set Gradle version "([^"]*)", build step description "([^"]*)" and tasks "([^"]*)"$/ do |version, description, tasks|
  @step.version version
  @step.description description
  @step.tasks tasks
end
