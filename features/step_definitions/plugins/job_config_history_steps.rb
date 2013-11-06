Then /^jobConfigHistory page should show difference/ do
  sleep 1
  all(:xpath, "//tr//a[contains(text(),'View as XML')]").length.should be >= 2
  all(:xpath, "//tr//a[contains(text(),'(RAW)')]").length.should be >= 2
end

When /^I dispaly difference/ do
  # There are two buttons
  first(:xpath, "//button[text()='Show Diffs']").click
end

Then /^configuration should have "([^"]*)" instead of "([^"]*)"/ do |current, original|
  page.should have_xpath("//td[@class='diff_original']/pre[normalize-space(text())='#{original}']")
  page.should have_xpath("//td[@class='diff_revised']/pre[normalize-space(text())='#{current}']")
end
