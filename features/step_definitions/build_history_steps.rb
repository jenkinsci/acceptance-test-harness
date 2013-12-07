When /^I lock the build$/ do
  @job.last_build.open

  step %{I click the "Keep this build forever" button}
end

Then /^the global build history should show the build$/ do
  visit '/view/All/builds'
  page.should have_content("#{@job.name} #1")
end

Then /^the job (should|should not) have build (\d+)$/ do |should_or_not, buildNumber|
  @job.build(buildNumber).open

  hold_the_build = have_content("Build ##{buildNumber}")
  page.send should_or_not, hold_the_build
end

Then /^the job should have (\d+) builds?$/ do |count|
  (@job.next_build_number - 1).should eq count.to_i
end

Then /^the job should be built on "(.*?)"$/ do |name|
  @job.last_build.wait_until_finished
  $jenkins.node(name).build_history.should include @job
end
