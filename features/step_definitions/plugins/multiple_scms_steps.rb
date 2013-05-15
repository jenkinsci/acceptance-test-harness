When /^I choose Multiple SCMs SCM$/ do
  @multiscm = @job.add_scm 'Multiple SCMs'
end

When /^I add Git scm url "([^"]*)" and directory name "([^"]*)"$/ do |url, dirname|
  @gitscm = @multiscm.add 'Git'
  @gitscm.url url
  @gitscm.local_dir dirname
end

When /^I add Subversion scm url "([^"]*)" and directory name "([^"]*)"$/ do |url, dirname|
  @svnscm = @multiscm.add "Subversion"
  @svnscm.url url
  @svnscm.local_dir dirname
end
