When /^I install the "(.*?)" plugin from the update center$/ do |plugin|
  Jenkins::PluginManager.new(@base_url, nil).install_plugin! plugin
end

Given /^I have installed the "(.*?)" plugin$/ do |plugin|
  Jenkins::PluginManager.new(@base_url, nil).install_plugin! plugin
end

Then /^the job should be able to use the "(.*?)" SCM$/ do |scm|
  page.should have_content scm
end

Then /^plugin page "([^"]*)" should exist$/ do |page|
  visit "#{@baseurl}/plugin/#{page}"
end
