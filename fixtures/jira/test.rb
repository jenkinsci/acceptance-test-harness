#!/usr/bin/env ruby
require 'rubygems'
require 'pp'
require 'jira'

# Consider the use of :use_ssl and :ssl_verify_mode options if running locally 
# for tests.

username = "admin"
password = "admin"

options = {
            :username => username,
            :password => password,
            :site     => 'http://localhost:49153/',
            :context_path => '/jira',
            :auth_type => :basic,
            :use_ssl => false
          }

client = JIRA::Client.new(options)

# Show all projects
projects = client.Project.all

projects.each do |project|
  puts "Project -> key: #{project.key}, name: #{project.name}"

end


