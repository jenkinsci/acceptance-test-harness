Then /^the global build history should show the build$/ do
  visit '/view/All/builds'
  page.should have_content("#{@job.name} #1")
end
