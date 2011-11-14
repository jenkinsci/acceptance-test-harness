#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

Then /^the page should say "([^"]*)"$/ do |content|
  page.should have_content(content)
end
