When /^I check out code from Subversion repository "([^"]*)"$/ do |url|
  @job.find(:xpath,"//input[@type='radio'][following-sibling::label[1]/text()='Subversion']").click
  @job.find(:xpath,"//input[@id='svn.remote.loc']").set(url)
end