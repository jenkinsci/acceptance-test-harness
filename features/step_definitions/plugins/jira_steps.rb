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

When /^the build should link to JIRA ([^ ]+) ticket$/ do |ticket|
  @job.last_build.open
  find_link(ticket).click  # make sure you can jump to it
end

When /^JIRA ([^ ]+) ticket has comment from admin that refers to the build$/ do |ticket|
  build_url = @job.build(@job.last_build.json['number']).url
  comments = @docker['jira'].soap.get_comments_for_issue_with_key(ticket)

  raise "matching comment not found that links to #{build_url}" unless comments.find do |comment|
    comment.body =~ /#{build_url}/
  end
end