When /^I check out code from Git repository "([^"]*)"$/ do |url|
  @job.find(:xpath,"//input[@path='/scm[1]']").click
  @job.find(:xpath,"//input[@path='/scm[1]/userRemoteConfigs/url']").set(url)
end

When /^I setup branch specifier to "([^"]*)"$/ do |branch|
  @job.find(:xpath,"//input[@path='/scm[1]/branches/name']").set(branch)
end

When /^I navigate to Git Advanced section$/ do 
  @job.find(:xpath,"//button[@path='/scm[1]/advanced-button']").click
end

When /^I setup local branch to "([^"]*)"$/ do |branch|
  @job.find(:xpath,"//input[@path='/scm[1]/localBranch']").set(branch)
end

When /^I setup local Git repo dir to "([^"]*)"$/ do |dir|
  @job.find(:xpath,"//input[@path='/scm[1]/relativeTargetDir']").set(dir)
end

When /^I navigate to Git Remote config Advanced section$/ do 
  @job.find(:xpath,"//button[@path='/scm[1]/userRemoteConfigs/advanced-button']").click
end

When /^I setup Git repo name to "([^"]*)"$/ do |repo_name|
  @job.find(:xpath,"//input[@path='/scm[1]/userRemoteConfigs/name']").set(repo_name)
end



