def install_plugin!(plugin)
  manager = Jenkins::PluginManager.new(@base_url, nil)
  unless manager.installed?(plugin)
    manager.install_plugin plugin
    found = @runner.log_watcher.wait_until_logged(/Installation successful: #{plugin}/i)
    found.should be true
  end
  manager.installed?(plugin).should be true
end

When /^I install the "(.*?)" plugin from the update center$/ do |plugin|
  install_plugin! plugin
end

Given /^I have installed the "(.*?)" plugin$/ do |plugin|
  install_plugin! plugin
end

Then /^the job should be able to use the Git SCM$/ do
  page.should have_content 'Git'
end

Then /^the build should fail$/ do
  pending # express the regexp above with the code you wish you had
end

