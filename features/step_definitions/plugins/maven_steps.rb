Given /^a Maven$/ do
  @maven = Jenkins::Maven.new(@basedir, "Maven")
end

Given /^a Maven job$/ do
  @job = Jenkins::Job.create('Maven', @base_url)
  @step = @job.step
end

Given /^I have default Maven configured$/ do
  step 'I have Maven "3.0.4" auto-installation named "default" configured'
end

Given /^I have Maven "([^"]*)" auto-installation named "([^"]*)" configured$/ do |version, name|
  @runner.wait_for_updates 'Maven'
  @maven = Jenkins::Maven.new(@basedir, "Maven")
  # @maven.prepare_autoinstall(@runner)
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Maven")
    @maven.add_auto_installation(name, version)
  end
end

Given /^I have Maven "([^"]*)" installed in "([^"]*)" configured$/ do |name, maven_home|
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

When /^I add a top-level maven target$/ do |goals|
  step %{I add a top-level maven target "#{goals}"}
end

When /^I add a top-level maven target "([^"]*)"$/ do |goals|
  if !@job.is_a? Jenkins::MavenJob
    @step = @job.add_build_step 'Maven'
  end

  @step.goals goals
end

When /^I add a top-level maven target for maven "([^"]*)"$/ do |version, goals|
  if !@job.is_a? Jenkins::MavenJob
    @step = @job.add_build_step 'Maven'
  end

  @step.goals goals
  @step.version version
end

When /^I use local Maven repository$/ do
  @step.use_local_repo
end

When /^I set maven options "([^"]+)"$/ do |opts|
  if @job.is_a? Jenkins::MavenJob
    @job.options opts
  else
    raise "Maven job only"
  end
end

When /^I set maven properties$/ do |properties|
  @step.properties properties
end

When /^I set global MAVEN_OPTS "([^"]*)"$/ do |opts|
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @maven.use_global_options opts
  end
end

Then /^the job should have module named "([^"]+)"$/ do |module_name|

  # Module action should exist
  build = @job.last_build.wait_until_finished
  maven_module = @job.module module_name
  maven_module.should exist

  module_build = build.module module_name
  module_build.should exist

  # Modules action should have correct module links
  build.open
  first(:xpath, "//a[@href='#{maven_module.url_chunk}/']").click
  page.current_url.should eq(build.module(module_name).url)

  visit @job.url + '/modules'

  xpath = "//a[@href='#{maven_module.url_chunk}/']"
  find(:xpath, xpath).click
  page.current_url.should eq(maven_module.url)

  page.should have_content "Project name: #{@job.name}/#{maven_module.name}"
end
