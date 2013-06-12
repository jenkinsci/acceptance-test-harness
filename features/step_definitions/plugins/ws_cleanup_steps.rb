Then /^there (should|should not) be "([^"]*)" in the workspace$/ do |should_or_not, filename|
  @job.workspace.contains(filename).send should_or_not, eql(true)
end
