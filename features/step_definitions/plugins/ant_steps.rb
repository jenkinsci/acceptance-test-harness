#!/usr/bin/env ruby

When /^I select Ant named "([^\"]*)"$/ do |name|
  @job.configure do
    find(:xpath, "//select[@name='ant.antName']").locate.click
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

Given /^fake Ant installation at "([^"]*)"$/ do |path|
  # where is the real ant?
  real = ENV['PATH'].split(':').find { |p| File.exists? "#{p}/ant" }

  FileUtils.mkdir_p "#{path}/bin"
  ant="#{path}/bin/ant"
  open(ant,'wb') do |f|
    f.write """#!/bin/sh
echo fake ant at $0
export ANT_HOME=
exec #{real}/ant \"$@\"
"""
  end
  File.chmod(0755,ant)
end
