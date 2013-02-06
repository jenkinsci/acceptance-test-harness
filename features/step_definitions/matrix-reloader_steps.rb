When /^I configure user axis "([^"]*)" with values "([^"]*)"$/ do |name, value|
  if !(page.current_url.eql? @job.configure_url)
      visit @job.configure_url
  end  
  find(:xpath, "//button[text()='Add axis']").click
  find(:xpath, "//li/a[text()='User-defined Axis']").click
  sleep 0.1 # wait until axis appear
  input = "//div[@name='axis' and @descriptorid='hudson.matrix.TextAxis']//td/input";
  find(:xpath, "(#{input}[@name='_.name'])[last()]").set(name)
  find(:xpath, "(#{input}[@name='_.valueString'])[last()]").set(value)
end

Then /^I should see matrix configuration "([^"]*)"$/ do |configuration|
  page.should have_xpath("//input[@name='MRP::#{configuration}']")
end

When /^I select matrix configuration "([^"]*)"$/ do |configuration|
  find(:xpath, "//input[@name='MRP::#{configuration}']").set(true)
end

When /^I rebuild matrix job$/ do
  find(:xpath, "//form[@action='configSubmit']//button[text()='Rebuild Matrix']").click
end

Then /^combination "([^"]*)" (should|should not) be built$/ do |configuration, operator|
  visit @job.last_build.build_url
  build_title = find(:xpath,"//h1[contains(text(),'Build')]").text()
  build_name = build_title.split(" ")[2]
  visit @job.job_url + "/#{configuration}/lastBuild"

  haveBuiltConfig = have_xpath("//h1[contains(text(),'Build #{build_name}')]");
  if operator == "should"
    page.should haveBuiltConfig
  else
    page.should_not haveBuiltConfig
  end
end
