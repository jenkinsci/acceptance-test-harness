When /^I set up "([^"]*)" as the Checkstyle results$/ do |results|
  find(:path, '/publisher/pattern').set(results)
end

When /^I visit Checkstyle report$/ do
  @checkstyle = Plugins::Checkstyle.new(@job)
  visit @checkstyle.url
end


Then /^I should see there are (\d+) warnings$/ do |warn_number|
  expect(warn_number.to_i).to eq(@checkstyle.warnings_number)
end

Then /^I should see there are (\d+) new warnings$/ do |new_warn_number|
  expect(new_warn_number.to_i).to eq(@checkstyle.new_warnings_number)
end

Then /^I should see there are (\d+) fixed warnings$/ do |fixed_warn_number|
  expect(fixed_warn_number.to_i).to eq(@checkstyle.fixed_warnings_number)
end

Then /^I should see there are (\d+) high priority warnings$/ do |high_warn_number|
  expect(high_warn_number.to_i).to eq(@checkstyle.high_warnings_number)
end

Then /^I should see there are (\d+) normal priority warnings$/ do |normal_warn_number|
  expect(normal_warn_number.to_i).to eq(@checkstyle.normal_warnings_number)
end

Then /^I should see there are (\d+) low priority warnings$/ do |low_warn_number|
  expect(low_warn_number.to_i).to eq(@checkstyle.low_warnings_number)
end

