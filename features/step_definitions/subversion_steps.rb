When /^I check out code from Subversion repository "([^"]*)"$/ do |url|
  # older version put <label> next to radio, recent version put label as the parent of radio
  @job.find(:xpath,"//input[@type='radio'][(following-sibling::label[1]/text()='Subversion') or (normalize-space(../text())='Subversion')]").click
  @job.find(:xpath,"//input[@id='svn.remote.loc']").set(url)
end