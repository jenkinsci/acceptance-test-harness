Then /^jobConfigHistory page should show difference/ do
  all(:xpath, "//tr//a[contains(text(),'View as XML')]").length.should >= 2
  all(:xpath, "//tr//a[contains(text(),'(RAW)')]").length.should >= 2
end

When /^I dispaly difference/ do
  find(:xpath, "//button[text()='Show Diffs']").click
end

Then /^configuration should have "([^"]*)" instead of "([^"]*)"/ do |current, original|
  page.should have_xpath("//td[@class='diff_original']/pre[normalize-space(text())='#{original}']")
  page.should have_xpath("//td[@class='diff_revised']/pre[normalize-space(text())='#{current}']")
end
