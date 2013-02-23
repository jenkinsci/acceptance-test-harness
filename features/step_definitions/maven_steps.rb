Given /^I add Maven version "([^"]*)" with name "([^"]*)" installed automatically to Jenkins config page$/ do |version, name|
  @maven = Jenkins::Maven.new(@basedir, "Maven")
  # @maven.prepare_autoinstall(@runner)
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Maven")
    @maven.add_auto_installation(name, version)
  end
end

Given /^I add Maven with name "([^"]*)" and Maven home "([^"]*)" to Jenkins config page$/ do |name, maven_home|
  @maven = Jenkins::Maven.new(@basedir, "Maven")
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Maven")
    @maven.add_local_installation(name, maven_home)
  end
end

Given /^fake Maven installation at "([^"]*)"$/ do |path|
  # where is the real maven?
  real = ENV['PATH'].split(':').find { |p| File.exists? "#{p}/mvn" }

  FileUtils.mkdir_p "#{path}/bin"
  maven="#{path}/bin/mvn"
  open(maven,'wb') do |f|
    f.write """#!/bin/sh
echo fake maven at $0
export M2_HOME=
exec #{real}/mvn \"$@\"
"""
  end
  File.chmod(0755,maven)
end

When /^I add a top-level maven target "([^"]*)" for maven "([^"]*)"$/ do |goals, version|
  @maven.add_maven_step(version, goals)
end

