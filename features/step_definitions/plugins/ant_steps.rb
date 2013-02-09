#!/usr/bin/env ruby

When /^I select Ant named "([^\"]*)"$/ do |name|
  @job.configure do
    @job.locate("//select[@name='ant.antName']").click
    find(:xpath, "//option[@value='#{name}']").click
  end
end

When /^I add an Ant build step for:$/ do |ant_xml|
  @job.configure do
    @job.add_script_step("cat > build.xml << EOF \n #{ant_xml} \nEOF")
    Plugins::Ant.add_ant_step('hello', 'build.xml')
  end
end

When /^I add Ant version "([^"]*)" with name "([^"]*)" installed automatically to Jenkins config page$/ do |version, name|
  @ant_plugin = Plugins::Ant.new(@basedir, "Ant plugin")
  @ant_plugin.prepare_autoinstall(@runner)
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Ant")
    Plugins::Ant.add_auto_installation(name, version)
  end
end

When /^I add Ant version with name "([^"]*)" and Ant home "([^"]*)" to Jenkins config page$/ do |name, ant_home|
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Ant")
    Plugins::Ant.add_local_installation(name, ant_home)
  end
end


