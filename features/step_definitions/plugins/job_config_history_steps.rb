Then /^jobConfigHistory page should show difference/ do
  page.should have_xpath("//tr[3]//a[contains(text(),'View as XML')]")
  page.should have_xpath("//tr[5]//a[contains(text(),'View as XML')]")
end

When /^I dispaly difference/ do
  find(:xpath, "//button[text()='Show Diffs']").click
end

Then /^configuration should have "([^"]*)" instead of "([^"]*)"/ do |current, original|
  page.should have_xpath("//td[@class='diff_original']/pre[normalize-space(text())='#{original}']")
  page.should have_xpath("//td[@class='diff_revised']/pre[normalize-space(text())='#{current}']")
end
