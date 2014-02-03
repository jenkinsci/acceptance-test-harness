Given /^a view$/ do
  step %{a view named "#{Jenkins::View.random_name}"}
end

Given /^a view named "(.*?)"$/ do |name|
  @view = Jenkins::View.create_view(@base_url, name, "List View")
end

When /^I create a view with a type "([^"]*)" and name "([^"]*)"$/ do |type, name|
  @view = Jenkins::View.create_view(@base_url, name, type)
end

When /^I create a view named "(.*?)"$/ do |name|
  @view = Jenkins::View.create_view(@base_url, name, "List View")
end

When /^I create job "(.*?)" in the view$/ do |name|
  @job = Jenkins::Job.create_named 'FreeStyle', @view.url, name
end

When /^I save the view$/ do
  click_button "OK"
end

When /^I visit the view page$/ do
  @view.open
end

When /^I build "([^"]*)" in view$/ do |job|
  if @view.nil?
    visit @base_url
  else
    @view.open
  end

  find(:xpath, "//a[contains(@href, '/#{job}/build?')]/img[contains(@title, 'Schedule a build')]").click
end

Then /^I should see the view on the main page$/ do
  visit(@base_url)
  page.should have_xpath("//table[@id='viewList']//a[text()='#{@view.name}']")
end
