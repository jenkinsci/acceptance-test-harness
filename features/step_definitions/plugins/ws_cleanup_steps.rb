Then /^I should be able to configure workspace cleanup$/ do
  @job.configure
  page.should have_xpath("//input[@name='hudson-plugins-ws_cleanup-PreBuildCleanup']");
end

Then /^there (should|should not) be "([^"]*)" in the workspace$/ do |operator, filename|
  present = @job.workspace.contains(filename)
  expected = operator == 'should'
  present.should eql(expected)
end
