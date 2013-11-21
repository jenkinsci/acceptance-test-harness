When /^I add marker post-build script$/ do

  @marker = Jenkins::PageObject.random_name

  @job.configure do
    postBuildStep = Plugins::PostBuildScript::Publisher.add(@job)
    shell_step = postBuildStep.add_step 'Shell'
    shell_step.command "echo '#{@marker}'"
  end
end

When /^I allow the script to run only for builds that (failed|succeeded)$/ do |status|

  checkboxes = {
      'failed' => 'scriptOnlyIfFailure',
      'succeeded' => 'scriptOnlyIfSuccess',
  }

  @job.configure do
    check checkboxes[status]
  end
end

Then /^the post-build script (should|should not) have been executed$/ do |should_or_not|
  @job.last_build.console.send should_or_not, include(@marker)
end
