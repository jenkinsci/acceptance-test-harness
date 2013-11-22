When /^"([^"]*)" project on docker jira fixture$/ do |name|
  c = @docker['jira']
  c.wait_for_ready
  c.create_project(name)
end