When /^I install the "(.*?)" plugin from the update center$/ do |plugin|
  manager = Jenkins::PluginManager.new(@base_url, nil)
  manager.install_plugin plugin
  found = @runner.log_watcher.wait_until_logged(/Installation successful: #{plugin}/i)
  found.should be true
  manager.installed?(plugin).should be true
end

Then /^the job should be able to use the Git SCM$/ do
  page.should have_content 'Git'
end

