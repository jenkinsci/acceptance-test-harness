When /^I check out code from Git repository "([^"]*)"$/ do |url|
  find(:path,"/scm[1]").click
  find(:path,"/scm[1]/userRemoteConfigs/url").set(url)
end

When /^I setup branch specifier to "([^"]*)"$/ do |branch|
  find(:path,"/scm[1]/branches/name").set(branch)
end

When /^I navigate to Git Advanced section$/ do 
  find(:path,"/scm[1]/advanced-button").click
end

When /^I setup local branch to "([^"]*)"$/ do |branch|
  find(:path,"/scm[1]/localBranch").set(branch)
end

When /^I setup local Git repo dir to "([^"]*)"$/ do |dir|
  find(:path,"/scm[1]/relativeTargetDir").set(dir)
end

When /^I navigate to Git Remote config Advanced section$/ do 
  find(:path,"/scm[1]/userRemoteConfigs/advanced-button").click
end

When /^I setup Git repo name to "([^"]*)"$/ do |repo_name|
  find(:path,"/scm[1]/userRemoteConfigs/name").set(repo_name)
end



