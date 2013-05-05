When /^I check out code from Git repository "([^"]*)"$/ do |url|
  @gitscm = @job.add_scm 'Git'
  @gitscm.url url
end

When /^I setup branch specifier to "([^"]*)"$/ do |branch|
  @gitscm.branch branch
end

When /^I setup local branch to "([^"]*)"$/ do |branch|
  @gitscm.local_branch branch
end

When /^I setup local Git repo dir to "([^"]*)"$/ do |dir|
  @gitscm.local_dir dir
end

When /^I setup Git repo name to "([^"]*)"$/ do |repo_name|
  @gitscm.repo_name repo_name
end
