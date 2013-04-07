
When /^I set up "([^"]*)" as the FindBugs results$/ do |results|
  find(:path, '/publisher/pattern').set(results)
end
