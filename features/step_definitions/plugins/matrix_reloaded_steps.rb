Then /^I should see matrix configuration "([^"]*)"$/ do |configuration|
  page.should have_xpath("//input[@name='MRP::#{configuration}']")
end

When /^I select matrix configuration "([^"]*)"$/ do |configuration|
  find(:xpath, "//input[@name='MRP::#{configuration}']").set(true)
end

When /^I rebuild matrix job$/ do
  find(:xpath, "//form[@action='configSubmit']//button[text()='Rebuild Matrix']").click
end
