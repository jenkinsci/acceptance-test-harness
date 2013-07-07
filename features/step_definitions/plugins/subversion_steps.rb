When /^I check out code from Subversion repository "([^"]*)"$/ do |url|
  @svnscm = @job.add_scm 'Subversion'
  @svnscm.url url
end
