When /^I set groovy script from file "([^"]*)"$/ do |script|
  @job.find(:xpath,"//input[@type='radio'][(following-sibling::label[1]/text()='Groovy script file') or (normalize-space(../text())='Groovy script file')]").click
  @job.find(:xpath,"//input[@name='groovy.scriptFile']").set(script)
end

When /^I set groovy script "([^"]*)"$/ do |file|
  @job.find(:xpath,"//input[@type='radio'][(following-sibling::label[1]/text()='Groovy command') or (normalize-space(../text())='Groovy command')]").click
  @job.find(:xpath,"//textarea[@name='groovy.command']").set(file)
end

When /^I set groovy build step "([^"]*)"$/ do |step_name|
  @job.ensure_config_page
  page.execute_script "window.scrollTo(0, document.body.scrollHeight)"
  find(:xpath, "//button[text()='Add build step']").click
  find(:xpath, "//a[text()='#{step_name}']").click
end
