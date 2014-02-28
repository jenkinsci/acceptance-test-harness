Then /^there (should|should not) be "([^"]*)" in the workspace$/ do |should_or_not, filename|
  @job.last_build.wait_until_finished
  @job.workspace.contains(filename).send should_or_not, eql(true), "Artifact not present"
end
