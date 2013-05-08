When /^I configure violations reporting$/ do
  @job.add_postbuild_action 'Report Violations'
end

When /^I set "([^"]+)" reporter applied at "([^"]+)"$/ do |kind, pattern|
  find(:xpath, "//input[@name='#{kind}.pattern']").set(pattern)
end

Then /^there should be (\d+) "([^"]+)" violations in (\d+) files$/ do |count, kind, file_count|
  step %{I visit build action named "Violations"}
  row = find(:xpath, "//td//a[@href='##{kind}' and text()='#{kind}']/../..")
  violations = row.find(:xpath, "./td[2]").text.to_i
  files = row.find(:xpath, "./td[3]").text.to_i

  violations.should eq(count.to_i)
  files.should eq(file_count.to_i)
end
