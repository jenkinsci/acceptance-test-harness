When /^I set path to the pmd result "([^"]*)"$/ do |path|
  find(:xpath, "//div[@descriptorid='hudson.plugins.pmd.PmdPublisher']//input[@name='_.pattern']").set(path)
end

Then /^build page should has pmd summary "([^"]*)"$/ do |content|
  @job.last_build.open
  page.should have_content "#{content}"
end

Then /^I set publish always pdm$/ do
  find(:xpath, "//div[@descriptorid='hudson.plugins.pmd.PmdPublisher']//button[text()='Advanced...']").click
  find(:xpath, "//div[@descriptorid='hudson.plugins.pmd.PmdPublisher']//input[@name='canRunOnFailed']").set(true)
end
