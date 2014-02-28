Given /^I have Groovy "([^"]*)" auto-installation named "([^"]*)" configured$/ do |version, name|
  @runner.wait_for_updates 'Groovy'
  $jenkins.configure do
    tool = $jenkins.configure.add_tool_installer 'Groovy'
    tool.name = name
    tool.install_version = version
  end
end

Given /^I have Groovy "([^"]*)" installed in "([^"]*)" configured$/ do |name, groovy_home|
  $jenkins.configure do
    tool = $jenkins.configure.add_tool_installer 'Groovy'
    tool.name = name
    tool.home = groovy_home
  end
end

When /^I add system groovy script step$/ do |script|
  step = @job.add_build_step 'System groovy'
  step.command = script
end

When /^I add groovy script step$/ do |script|
  step = @job.add_build_step 'Groovy'
  step.command = script
end

When /^I add groovy script step using "(.*?)"$/ do |groovy, script|
  step = @job.add_build_step 'Groovy'
  step.version = groovy
  step.command = script
end

When /^I add groovy file step "(.*?)"$/ do |path|
  step = @job.add_build_step 'Groovy'
  step.file = path
end

# this needs preinstalled groovy to work
Given /^fake Groovy installation at "([^"]*)"$/ do |path|
  real = ENV['PATH'].split(':').find { |p| File.exists? "#{p}/groovy" }

  FileUtils.mkdir_p "#{path}/bin"
  groovy="#{path}/bin/groovy"
  open(groovy,'wb') do |f|
    f.write """#!/bin/sh
echo fake groovy at $0
export GROOVY_HOME=
exec #{real}/groovy \"$@\"
"""
  end
  File.chmod(0755,groovy)
end
