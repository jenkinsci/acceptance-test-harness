When /^I update disk usage$/ do
  @disk_usage = Plugins::DiskUsage.new(@basedir, "Disk usage plugin")
  @disk_usage.update
end

When /^I enable disk usage graph on the project page$/ do
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    find(:xpath, "//input[@path='/hudson-plugins-disk_usage-DiskUsageProjectActionFactory/showGraph']").check
  end
end

Then /^the disk usage should be updated$/ do
  @disk_usage.wait_for_update(@runner.log_watcher)
end

Then /^the project page should contain disk usage graph$/ do
  step %{I visit the job page}
  # changed in v0.22 and v0.23
  page.should have_xpath("//img[@src='disk-usage/graph/png' or starts-with(@src, 'diskUsage/graph/png')]")
end

Then /^the job workspace should occupy some space$/ do
  @job.open
  page.should have_text /Workspace [1-9]\d*,/
end

Then /^the job workspace should occupy no space$/ do
  @job.open
  page.should have_text "Workspace 0,"
end
