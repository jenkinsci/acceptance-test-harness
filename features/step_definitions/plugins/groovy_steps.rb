When /^I set groovy script from file "([^"]*)"$/ do |script|
  @job.find(:xpath,"//input[@type='radio'][(following-sibling::label[1]/text()='Groovy script file') or (normalize-space(../text())='Groovy script file')]").click
  @job.find(:xpath,"//input[@name='groovy.scriptFile']").set(script)
end

When /^I set groovy script "([^"]*)"$/ do |file|
  @job.find(:xpath,"//input[@type='radio'][(following-sibling::label[1]/text()='Groovy command') or (normalize-space(../text())='Groovy command')]").click
  @job.find(:xpath,"//textarea[@name='groovy.command']").set(file)
end

When /^I set groovy build step "([^"]*)"$/ do |step_name|
  @job.ensure_config_page
  page.execute_script "window.scrollTo(0, document.body.scrollHeight)"
  find(:xpath, "//button[text()='Add build step']").click
  find(:xpath, "//a[text()='#{step_name}']").click
end

When /^I select groovy named "([^\"]*)"$/ do |name|
  @job.ensure_config_page
  find(:xpath, "//select[@name='groovy.groovyName']").locate.click
  find(:xpath, "//option[@value='#{name}']").click
end

When /^I add Groovy version "([^"]*)" with name "([^"]*)" installed automatically to Jenkins config page$/ do |version, name|
  # @groovy_plugin = Plugins::Groovy.new(@basedir, "Groovy plugin")
  # @groovy_plugin.prepare_autoinstall(@runner)
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Groovy")
    Plugins::Groovy.add_auto_installation(name, version)
  end
end

When /^I add Groovy version with name "([^"]*)" and Groovy home "([^"]*)" to Jenkins config page$/ do |name, groovy_home|
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Groovy")
    Plugins::Groovy.add_local_installation(name, groovy_home)
  end
end

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
