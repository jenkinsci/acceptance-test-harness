When /^"([^"]*)" project on docker jira fixture$/ do |name|
  p = Plugins::JIRA::JIRA.new(@docker['jira'])
  p.wait_for_ready
  pending   # TODO: actually create a project
end