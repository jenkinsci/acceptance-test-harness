Given /^I have installed the "(.*?)" plugin$/ do |plugin|
  begin
    Jenkins::PluginManager.new(@base_url, nil).install_plugin! plugin
  rescue Jenkins::RestartNeeded
    @runner.restart
  end
end

When /^I install the "(.*?)" plugin from the update center$/ do |plugin|
  step %{I have installed the "#{plugin}" plugin}
end

Then /^plugin page "([^"]*)" should exist$/ do |page|
  visit "#{@baseurl}/plugin/#{page}"
end
