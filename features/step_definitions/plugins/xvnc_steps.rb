When /^I let xvnc run during the build$/ do
  find(:path, '/hudson-plugins-xvnc-Xvnc').check
end

When /^I let xvnc run during the build taking screanshot at the end$/ do
  step 'I let xvnc run during the build'
  find(:path, '/hudson-plugins-xvnc-Xvnc/takeScreenshot').check
end

When /^I set xvnc display number to (\d+)$/ do |displayNumber|
  jenkins = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  jenkins.configure do
    # xvnc 1.11 and earlier
    # find(:path, '/hudson-plugins-xvnc-Xvnc/baseDisplayNumber').set(displayNumber)
    find(:path, '/hudson-plugins-xvnc-Xvnc/minDisplayNumber').set(displayNumber)
    find(:path, '/hudson-plugins-xvnc-Xvnc/maxDisplayNumber').set(displayNumber)
  end
end

Then /^xvnc run during the build$/ do
  console = @job.last_build.console
  console.should have_content 'Starting xvnc'
  console.should have_content 'Killing Xvnc process ID'
end

Then /^took a screanshot$/ do
  @job.last_build.console.should have_content 'Taking screenshot'
  step %{the artifact "screenshot.jpg" should be archived}
end

Then /^used display number (\d+)$/ do |displayNumber|
  @job.last_build.console.should have_content " :#{displayNumber} "
end
