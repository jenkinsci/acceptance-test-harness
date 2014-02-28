When /^I add console parser for "([^"]*)"$/ do |tool|
  find(:path, '/publisher/repeatable-add').click
  find(:xpath, "//select[@path='/publisher/consoleParsers/parserName']/option[text()='#{tool}']")
      .select_option
end

When /^I add workspace parser for "([^"]*)" applied at "([^"]*)"$/ do |tool, pattern|
  find(:path, '/publisher/repeatable-add[1]').click
  find(:path, '/publisher/parserConfigurations/pattern').set(pattern)
  find(:xpath, "//select[@path='/publisher/parserConfigurations/parserName']/option[text()='#{tool}']")
      .select_option
end


Then /^build should have (\d+) "([^"]+)" warnings?$/ do |count, tool|
  @job.last_build.wait_until_finished

  should_or_not = count.to_i > 0 ? 'should' : 'should not'

  step %{the job #{should_or_not} have "#{tool} Warnings" action}
  step %{the build #{should_or_not} have "#{tool} Warnings" action}
  @job.last_build.open
  step %{the page should say "#{tool} Warnings: #{count}"};
end
