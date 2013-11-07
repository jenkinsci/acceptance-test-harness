When /^I add marker post-build script$/ do

  @marker = Jenkins::PageObject.random_name

  @job.configure do
    begin
      @job.add_postbuild_action 'Execute a set of scripts'
    rescue Capybara::ElementNotFound
      # The prefix was stripped in 0.12
      find(:xpath, "//a[text()='[PostBuildScript] - Execute a set of scripts']").click
    end
    step = find(:path, '/publisher/hetero-list-add[buildStep]').click
    find(:path, '/publisher').click_link 'Execute shell'
    find(:path, '/publisher/buildStep/command').set("echo '#{@marker}'")
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
