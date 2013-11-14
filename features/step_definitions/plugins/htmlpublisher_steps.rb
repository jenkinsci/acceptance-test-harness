When /^I configure "(.*?)" directory to be published as "(.*?)"$/ do |dirname, title|
  @html_publisher ||= @job.add_postbuild_step 'Publish HTML'
  @html_publisher.add_report title, dirname
end

When /^I set index file to "(.*?)"$/ do |indexName|
  @html_publisher.lastReport.index = indexName
end

Then /^the html report "(.*?)" should be correclty published$/ do |title|
  visit @job.url + '/' + title.gsub(' ', '_')
  within_frame 'myframe' do
    find(:css, 'h1').should be_visible
    find(:css, 'p', :visible => false).should_not be_visible
  end
end
