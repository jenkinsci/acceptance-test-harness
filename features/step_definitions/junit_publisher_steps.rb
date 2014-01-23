When /^I set Junit archiver path "([^"]*)"$/ do |path|
  @job.ensure_config_page
  find(:xpath, "//button[text()='Add post-build action']").click
  find(:xpath, "//a[text()='Publish JUnit test result report']").click
  find(:xpath, "//input[@name='_.testResults']").set(path)
end

Then /^"(.*?)" error summary should match "(.*?)"$/ do |test, message|
  toggle test
  page.text.should include message
  toggle test
end

def toggle(test)
  xpath = "//a[text()='#{test}']/../a[starts-with(@href, 'javascript')]"
  elements = all(:xpath, xpath)
  elements.length.should be >= 1
  # JUnit report produced by TestNG contains colliding test names
  for e in elements do
    e.click
  end
end
