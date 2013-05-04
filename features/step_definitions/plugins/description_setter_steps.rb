When /^I set up "([^"]*)" as the description setter reg-exp$/ do |regexp|
  find(:path, '/publisher/regexp').set(regexp)
end

When /^I set up "([^"]*)" as the description setter description$/ do |description|
  find(:path, '/publisher/description').set(description)
end

Then /^the build should have description "([^"]*)"$/ do |description|
  @job.last_build.open
  desc = find(:xpath,'//div[@id="description"]/div')
  desc.should have_content(description)
end

Then /^the build should have description "([^"]*)" in build history$/ do |description|
  @job.open
  desc = find(:xpath,'//table[@id="buildHistory"]/tbody/tr/td[@class="desc"]')
  desc.should have_content(description)
end
