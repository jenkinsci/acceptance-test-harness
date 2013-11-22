When /^"([^"]*)" project on docker jira fixture$/ do |name|
  c = @docker['jira']
  c.wait_for_ready
  c.create_project(name)
end

Then /^I configure docker fixture as JIRA site$/ do
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    Plugins::JIRA::GlobalConfig.add(@docker['jira'].url,"admin","admin")
  end
end

When /^a new issue in "([^"]*)" project on docker jira fixture$/ do |project|
  @docker['jira'].create_issue project
end