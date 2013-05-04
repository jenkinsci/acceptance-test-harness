When /^I set up "([^"]*)" as the Cobertura report$/ do |report|
  find(:path, '/publisher/coberturaReportFile').set(report)
end
