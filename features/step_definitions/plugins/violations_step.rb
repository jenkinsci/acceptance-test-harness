When /^I configure violations reporting$/ do
  if @job.is_a? Jenkins::MavenJob
    find(:path, '/hudson-plugins-violations-hudson-maven-ViolationsMavenReporter').check
  else
    @job.add_postbuild_action 'Report Violations'
  end
end

When /^I set "([^"]+)" reporter applied at "([^"]+)"$/ do |kind, pattern|
  find(:xpath, "//input[@name='#{kind}.pattern']").set(pattern)
end

Then /^there should be (\d+) "([^"]+)" violations in (\d+) files$/ do |count, kind, file_count|
  step %{I visit build action named "Violations"}
  assert_violations count, kind, file_count
end

Then /^there should be (\d+) "([^"]+)" violations in (\d+) files for module "([^"]+)"$/ do |count, kind, file_count, module_name|
  module_url = @job.last_build.wait_until_finished.module(module_name).url

  visit "#{module_url}/violations"

  assert_violations count, kind, file_count
end

private
def assert_violations(count, kind, file_count)
  row = find(:xpath, "//td//a[@href='##{kind}' and text()='#{kind}']/../..")
  violations = row.find(:xpath, "./td[2]").text.to_i
  files = row.find(:xpath, "./td[3]").text.to_i

  violations.should eq(count.to_i)
  files.should eq(file_count.to_i)
end
