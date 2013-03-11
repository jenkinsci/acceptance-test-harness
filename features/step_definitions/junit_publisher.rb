When /^I add a script build step to prepare test results$/ do |script|
  @job.add_script_step("#{script}")
end

When /^I set Junit archiver path "([^"]*)"$/ do |path|
@job.ensure_config_page
 page.execute_script "window.scrollTo(0, document.body.scrollHeight)"
find(:xpath, "//button[text()='Add post-build action']").click
find(:xpath, "//a[text()='Publish JUnit test result report']").click
find(:xpath, "//input[@name='_.testResults']").set(path)
end
