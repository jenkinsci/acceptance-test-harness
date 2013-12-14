When /^I publish "(.*?)" report from "(.*?)"$/ do |kind, path|
  xunit = @job.add_postbuild_step 'xUnit'
  junit = xunit.add_tool kind
  junit.pattern = path
end
