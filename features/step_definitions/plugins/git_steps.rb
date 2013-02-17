When /^I check out code from Git repository "([^"]*)"$/ do |url|
  @job.find(:xpath,"//input[@path='/scm[1]']").click
  @job.find(:xpath,"//input[@path='/scm[1]/userRemoteConfigs/url']").set(url)
end

When /^I setup branch specifier to "([^"]*)"$/ do |branch|
  @job.find(:xpath,"//input[@path='/scm[1]/branches/name']").set(branch)
end

