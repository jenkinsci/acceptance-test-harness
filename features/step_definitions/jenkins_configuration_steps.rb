#!/usr/bin/env ruby

Given /^I add Java version "([^"]*)" with name "([^"]*)" installed automatically to Jenkins config page$/ do |version, name|
  @runner.wait_for_updates 'JDK'
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.enter_oracle_credentials(ENV['ORACLE_LOGIN'],ENV['ORACLE_PASSWORD'])
  @jenkins_config.configure do
    @jenkins_config.add_tool("JDK")
    @jenkins_config.add_jdk_auto_installation(name,version)
  end
end

Then /^I should be able to configure (.*) globally$/ do |section_name|
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.open
  page.should have_xpath "//div[@class='section-header' and normalize-space(text())='#{section_name}']"
end
