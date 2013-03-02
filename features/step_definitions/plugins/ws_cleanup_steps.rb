Then /^I should be able to configure workspace cleanup$/ do
  @job.configure
  page.should have_xpath("//input[@name='hudson-plugins-ws_cleanup-PreBuildCleanup']");
end

Then /^there (should|should not) be "([^"]*)" in the workspace$/ do |should_or_not, filename|
  @job.workspace.contains(filename).send should_or_not, eql(true)
end
