When /^I set up "([^"]*)" as the FindBugs results$/ do |results|
  find(:path, '/publisher/pattern').set(results)
end

Then /^job and build should have FindBugs action$/ do
  @job.last_build.wait_until_finished
  step %{the job should have "FindBugs Warnings" action}
  step %{the build should have "FindBugs Warnings" action}
end
