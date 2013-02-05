#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2
require 'pry'

When /^I visit the home page$/ do
  visit "/"
end

When /^I click the "([^"]*)" checkbox$/ do |name|
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

Then /^the page should say "([^"]*)"$/ do |content|
  page.should have_content(content)
end

When /^I debug$/ do
  binding.pry
end