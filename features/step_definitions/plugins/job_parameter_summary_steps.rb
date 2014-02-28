Then /^summary should contain String Parameter "([^"]+)" defaulting to "([^"]+)"$/ do |name, default|
  @job.open
  page.should have_xpath "//h2[text() = 'Build Parameters']"
  page.text.should match /String Parameter\s+#{name}\s+=\s+("?)#{default}\1/
end
