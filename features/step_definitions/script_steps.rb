When /^I execute system script$/ do |script|
  visit $jenkins.base_url + '/script'
  run script
end

When /^I execute system script on "(.*?)"$/ do |computer, script|
  visit $jenkins.node(computer).url + '/script'
  run script
end

When /^the system script output should match "(.*?)"$/ do |expected|
  find(:css, 'h2 + pre').text.should == expected
end

def run(script)
  textarea = Jenkins::Util::CodeMirror.new(page, '/script')
  textarea.set_content script

  click_button 'Run'
end
