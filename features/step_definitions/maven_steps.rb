Given /^a Maven$/ do
  @maven = Jenkins::Maven.new(@basedir, "Maven")
end

Given /^a Maven job$/ do
  @job = Jenkins::Job.create('Maven', @base_url)
end

Given /^I have default Maven configured$/ do
  step 'I add Maven version "3.0.4" with name "default" installed automatically to Jenkins config page'
end

Given /^I add Maven version "([^"]*)" with name "([^"]*)" installed automatically to Jenkins config page$/ do |version, name|
  @runner.wait_for_updates 'Maven'
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

When /^I add a top-level maven target "([^"]*)"$/ do |goals|
  if @job.is_a? Jenkins::MavenJob
    @job.goals goals
  else
    @maven.add_maven_step(goals: goals)
  end
end

When /^I add a top-level maven target "([^"]*)" for maven "([^"]*)"$/ do |goals, version|
  if @job.is_a? Jenkins::MavenJob
    @job.goals goals
    @job.version version
  else
    @maven.add_maven_step(goals: goals, version: version)
  end
end

When /^I use local Maven repository$/ do
  if @job.is_a? Jenkins::MavenJob
    @job.use_local_repo
  else
    @maven.use_local_repo
  end
end

When /^I set maven options "([^"]+)"$/ do |opts|
  if @job.is_a? Jenkins::MavenJob
    @job.options opts
  else
    raise "Maven job only"
  end
end

Then /^the job should have module named "([^"]+)"$/ do |module_name|

  # Module build page should exist
  module_page = @job.last_build.wait_until_finished.module(module_name)
  module_page.exists.should be

  # Build page should have module links
  @job.last_build.open
  page.should have_xpath "//a[@href='#{module_name}/']"

  # Modules action should have correct module links
  visit @job.job_url + '/modules'
  find(:xpath, "//a[contains(@href,'#{module_name}')]").click
  page.current_url.should be(module_page.url)
end
