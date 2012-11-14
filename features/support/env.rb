#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require 'capybara/cucumber'
require 'capybara/session'
require 'selenium-webdriver'
require 'sauce/cucumber'
require 'etc'

Capybara.register_driver :selenium do |app|
  http_client = Selenium::WebDriver::Remote::Http::Default.new
  http_client.timeout = 120
  Capybara::Selenium::Driver.new(app, :browser => :firefox, :http_client => http_client)
end

Capybara.run_server = false
Capybara.default_selector = :css
Capybara.default_driver = :selenium

if ENV['SAUCE_ACCESS_KEY']
  # TODO: how to select the browser to run the test with?
  puts "Using Sauce OnDemand"
  Capybara.default_driver = :sauce
  Sauce.config do |c|
    c[:username] = 'jenkinsci'
    c[:start_tunnel] = true
    c[:browser_version] = '16'
    c['selenium-version'] = '2.26.0'
    c['custom-data'] = { :by => Etc.getlogin }
  end
end

# Include Page objects:

PAGE_OBJECTS_BASE = File.dirname(__FILE__) + "/../../lib/"


Dir["#{PAGE_OBJECTS_BASE}/*.rb"].each do |name|
  require File.expand_path(name)
end
