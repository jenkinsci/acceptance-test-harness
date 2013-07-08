When /^I create a subview of the view with a type "([^"]*)" and name "([^"]*)"$/ do |type, name|
  visit("#{@view.url}/newView")
  fill_in "name", :with => name
  find(:xpath, "//input[following-sibling::label[child::b[text()='#{type}']]]").set(true)
  click_button "OK"
end

When /^I should see "([^"]*)" view as a subview of the view$/ do |name|
  @view.open
  page.should have_xpath("//table[@id='projectstatus']//a[text()='#{name}']")
end

When /^I configure subview "([^"]*)" as a default of the view$/ do |name|
  visit(@view.configure_url)
  find(:xpath, "//select[@name='defaultView']/option[text()='#{name}']").click
end

Then /^I should see "([^"]*)" subview as an active view$/ do |name|
  @view.open
  page.should have_xpath("//table[@id='viewList']//td[@class='active' and text()='#{name}']")
end

Then /^I should see "([^"]*)" subview as an inactive view$/ do |name|
  @view.open
  page.should have_xpath("//table[@id='viewList']//td[contains(@class,'inactive')]/a[text()='#{name}']")
end
