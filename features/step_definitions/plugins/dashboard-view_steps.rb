When /^I configure dummy dashboard$/ do
  @view.configure do
    find(:path, '/hetero-list-add[topPortlet]').click
    click_link 'Build statistics'

    find(:path, '/hetero-list-add[bottomPortlet]').click
    click_link 'Jenkins jobs list'
  end
end

Then /^the dashboard sould contain details of "(.*?)"$/ do |arg1|
  @view.open

  page.should have_text 'Build statistics'
  page.should have_text 'Jenkins jobs list'

  page.should have_link 'job_in_view'
  page.should have_link '#1'

  allSuccess = '//table[@id="statistics"]//td[text()="Success"]/../td[text()="100.0"]'
  page.should have_xpath allSuccess
end
