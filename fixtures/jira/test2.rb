#!/usr/bin/env ruby
require 'jiraSOAP'

jira = JIRA::JIRAService.new 'http://localhost:49176/jira/'
jira.login 'admin','admin'

#p = jira.project_with_key 'ABC'
#puts p
#puts p.inspect

#p = JIRA::Project.new()
#p.name = "API test"
#p.key = "API"
#p.lead_username = "admin"

#jira.create_project_with_project p

#issue = JIRA::Issue.new.tap do |i|
#  i.project_name = "ABC"
#  i.type_id = 1 # Bug
#  i.priority_id = 1 # whatever
#  i.summary = "Test ticket"
#  i.description = "Description"
#end
#
#i = jira.create_issue_with_issue issue
#puts i.key
#

i = jira.get_issue_with_key "ABC-1"
puts i.inspect

comments = jira.get_comments_for_issue_with_key "ABC-1"
puts comments[0].body
#comments.each do |c|
#  puts c.id
#  puts c.body
#end
