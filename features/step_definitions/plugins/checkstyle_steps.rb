When /^I set up "([^"]*)" as the Checkstyle results$/ do |results|
  find(:path, '/publisher/pattern').set(results)
end
