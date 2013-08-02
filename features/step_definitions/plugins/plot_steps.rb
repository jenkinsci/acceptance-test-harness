When /^I add plot "(.*?)" in group "(.*?)"$/ do |title, group|
  @plot_step = @job.add_postbuild_step 'Plot'
  @plot_step.group = group
  @plot_step.title = title
end

When /^I configure csv data source "(.*?)"$/ do |path|
  @plot_step.source('csv', path)
end

Then /^there should be a plot called "(.*?)" in group "(.*?)"$/ do |title, group|
  @job.last_build.wait_until_finished
  visit @job.url + '/plot'
  page.should have_xpath "//h1[contains(text(), '#{group}')]"
  page.should have_xpath "//select[@name='choice']/option[contains(text(), '#{title}')]"
end
