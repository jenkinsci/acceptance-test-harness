Given /^I add Maven version "([^"]*)" with name "([^"]*)" installed automatically to Jenkins config page$/ do |version, name|
  @maven = Jenkins::Maven.new(@basedir, "Maven")
  # @maven.prepare_autoinstall(@runner)
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    @jenkins_config.add_tool("Maven")
    @maven.add_auto_installation(name, version)
  end
end

When /^I add a top-level maven target "([^"]*)" for maven "([^"]*)"$/ do |goals, version|
  @maven.add_maven_step(version, goals)
end
