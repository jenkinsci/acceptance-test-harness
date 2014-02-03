#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2
require 'pry'

Transform /^(should|should not)$/ do |should_or_not|
  should_or_not.gsub(' ', '_').to_sym
end

When /^I visit the home page$/ do
  visit "/"
end

When /^I check the "([^"]*)" checkbox$/ do |name|
  find(:xpath, "//input[@name='#{name}']").set(true)
end

When /^I click the "([^"]*)" button$/ do |name|
  find(:xpath, "//button[text()='#{name}']").click
end

# Choose an option by value or text in select identified by it's visual label
When /^I select "(.*?)" as a "(.*?)"$/ do |choice, select|
  select = find(:xpath, "//td[@class='setting-name' and text()='#{select}']/../td[@class='setting-main']/select")
  option = select.find(:xpath, "option[@value='#{choice}' or text()='#{choice}']")
  option.select_option
end

When /^I debug$/ do
  binding.pry
end

When /^I wait for (\d+) seconds?$/ do |seconds|
  sleep seconds.to_i
end

When /^I close the error dialog$/ do
  click_link 'Close'
end

When /^I restart Jenkins/ do
  @runner.restart
end

Then /^the page (should|should not) say "([^"]*)"$/ do |should_or_not, content|
  page.send should_or_not, have_content(content)
end

Then(/^I will write the rest of the test$/) do
  pending
end

Then /^the error description should contain$/ do |text|
  find(:css, "#error-description pre").text.should include text
end
