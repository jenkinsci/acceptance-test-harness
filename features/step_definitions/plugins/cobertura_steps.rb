When /^I set up "([^"]*)" as the Cobertura report$/ do |report|
  find(:path, '/publisher/coberturaReportFile').set(report)
end

When /^I visit Cobertura report$/ do
  @cobertura = Plugins::Cobertura.new(@job)
  visit @cobertura.url
end


Then /^I should see the coverage of packages is (\d+)%$/ do |coverage|
  expect(coverage.to_i).to eq(@cobertura.packages_coverage)
end

Then /^I should see the coverage of files is (\d+)%$/ do |coverage|
  expect(coverage.to_i).to eq(@cobertura.files_coverage)
end

Then /^I should see the coverage of classes is (\d+)%$/ do |coverage|
  expect(coverage.to_i).to eq(@cobertura.classes_coverage)
end

Then /^I should see the coverage of methods is (\d+)%$/ do |coverage|
  expect(coverage.to_i).to eq(@cobertura.methods_coverage)
end

Then /^I should see the coverage of lines is (\d+)%$/ do |coverage|
  expect(coverage.to_i).to eq(@cobertura.lines_coverage)
end

Then /^I should see the coverage of conditionals is (\d+)%$/ do |coverage|
  expect(coverage.to_i).to eq(@cobertura.conditionals_coverage)
end
