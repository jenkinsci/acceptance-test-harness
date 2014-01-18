When /^I publish "(.*?)" report from "(.*?)"$/ do |kind, path|
  xunit = @job.add_postbuild_step 'xUnit'
  junit = xunit.add_tool kind
  junit.pattern = path
end

Then /^the job page should contain test result trend chart$/ do
  @job.open
  page.should have_xpath '//img[@alt="[Test result trend chart]"]'
end
