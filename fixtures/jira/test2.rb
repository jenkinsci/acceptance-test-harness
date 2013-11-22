#!/usr/bin/env ruby
require 'jiraSOAP'

jira = JIRA::JIRAService.new 'http://localhost:49153/jira/'
jira.login 'admin','admin'

p = jira.project_with_key 'TEST'
puts p
puts p.inspect

p = JIRA::Project.new()
p.name = "API test"
p.key = "API"
p.lead_username = "admin"

jira.create_project_with_project p