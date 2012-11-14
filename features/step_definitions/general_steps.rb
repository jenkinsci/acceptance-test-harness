#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2
require 'pry'

When /^I visit the home page$/ do
  visit "/"
end

When /^I click the "([^"]*)" checkbox$/ do |name|
  find(:xpath, "//input[@name='#{name}']").set(true)
end

Then /^the page should say "([^"]*)"$/ do |content|
  page.should have_content(content)
end

When /^I debug$/ do
  binding.pry
end