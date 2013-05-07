Given /^I add Gradle version "([^"]*)" with name "([^"]*)" installed automatically to Jenkins config page$/ do |version, name|
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

When /^I set Gradle script file name "([^"]*)"$/ do |name|
  find(:xpath, "//div[@descriptorid='hudson.plugins.gradle.Gradle']//input[@name='_.buildFile']").set(name)
end

When /^I set Gradle script direcotry path "([^"]*)"$/ do |path|
  find(:xpath, "//div[@descriptorid='hudson.plugins.gradle.Gradle']//input[@name='_.rootBuildScriptDir']").set(path)
end

When /^I set Gradle switches "([^"]*)"$/ do |switches|
  find(:xpath, "//div[@descriptorid='hudson.plugins.gradle.Gradle']//input[@name='_.switches']").set(switches)
end

When /^I set Gradle version "([^"]*)", build step description "([^"]*)" and tasks "([^"]*)"$/ do |version, description, tasks|
  find(:xpath, "//div[@descriptorid='hudson.plugins.gradle.Gradle']//input[@name='_.description']").set(description)
  find(:xpath, "//div[@descriptorid='hudson.plugins.gradle.Gradle']//input[@name='_.tasks']").set(tasks)
  find(:xpath, "//div[@descriptorid='hudson.plugins.gradle.Gradle']//select[@name='gradleName']/option[@value='#{version}']").click
end
