When /^I choose Multiple SCMs SCM$/ do
  @job.find(:xpath,"//input[@type='radio'][(following-sibling::label[1]/text()='Multiple SCMs') or (normalize-space(../text())='Multiple SCMs')]").click
end	  

When /^I add a new SCM$/ do
  find(:xpath, "//button[text()='Add SCM']").click()
end

When /^I choose new "([^"]*)" SCM$/ do |scm|
  find(:xpath,"//a[text()='#{scm}']").click
end

When /^I add Git scm url "([^"]*)" and directory name "([^"]*)"$/ do |url, name|
  find(:xpath, "//button[text()='Add SCM']").click()
  find(:xpath,"//a[text()='Git']").click
  sleep 1
  find(:xpath,"//div[@descriptorid='hudson.plugins.git.GitSCM']//input[@name='_.url']").set(url)
  find(:xpath, "(//div[@descriptorid='hudson.plugins.git.GitSCM']//button[text()='Advanced...'])[last()]").click()
  sleep 1
  find(:xpath, "//div[@descriptorid='hudson.plugins.git.GitSCM']//input[@name='_.relativeTargetDir']").set(name)
end

When /^I add Subversion scm url "([^"]*)" and directory name "([^"]*)"$/ do |url, name|
  find(:xpath, "//button[text()='Add SCM']").click()
  find(:xpath,"//a[text()='Subversion']").click()
  sleep 1
  find(:xpath,"//input[@id='svn.remote.loc']").set(url)
  find(:xpath,"//input[@name='_.local']").set(name)
end

Then /^I should see "([^"]*)" SCM option$/ do |scm|
  page.should have_xpath("//a[text()='#{scm}']")
end

