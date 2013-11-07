When /^I set Junit archiver path "([^"]*)"$/ do |path|
  @job.ensure_config_page
  find(:xpath, "//button[text()='Add post-build action']").click
  find(:xpath, "//a[text()='Publish JUnit test result report']").click
  find(:xpath, "//input[@name='_.testResults']").set(path)
end
