When /^I add a Publish javadoc post build step with path "([^"]*)"$/ do |path|
  @job.add_postbuild_action("Publish Javadoc")
  find(:path, "/publisher/javadocDir").set(path)
end

When /^I set retain javadoc for each sucessful build$/ do
  find(:xpath, "//input[@name='_.keepAll']").set(true)
end

When /^the javadoc should display "([^"]*)"$/ do |content|
  @job.open
  find(:xpath, "//div[@id='tasks']/div/a[text()='Javadoc']").click
  page.should have_content(content)
end


When /^I add an Javadoc build step for:$/ do |script|
  @job.add_script_step("#{script}")
end

When /^I add build steps to generate javadoc$/ do
  options = "archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B"
  click_button 'Add build step'
  click_link 'Invoke top-level Maven targets'
  find(:path, "/builder/targets").set(options)
  options = "javadoc:javadoc -f my-app/pom.xml"
  click_button 'Add build step'
  click_link 'Invoke top-level Maven targets'
  find(:path, "/builder[1]/targets").set(options)
  
end

When /^I add a top-level maven target to create project$/ do
  @maven.add_maven_step("archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B")
end

Then /^javadoc should display "([^"]*)" for default configuration$/ do |content|
  visit (@job.job_url + "/default")
  find(:xpath, "//div[@id='tasks']/div/a[text()='Javadoc']").click
  page.should have_content(content)
end

