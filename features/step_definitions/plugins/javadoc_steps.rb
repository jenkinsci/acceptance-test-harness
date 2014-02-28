When /^I add a Publish javadoc post build step with path "([^"]*)"$/ do |path|
  step = @job.add_postbuild_step 'Javadoc'
  step.dir path
end

When /^I add build steps to generate javadoc$/ do
  create_project = @job.add_build_step 'Maven'
  create_project.goals "archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B"

  generate_javadoc = @job.add_build_step 'Maven'
  generate_javadoc.goals "javadoc:javadoc -f my-app/pom.xml"
end

Then /^the javadoc should display "([^"]*)"$/ do |content|
  @job.open
  find(:xpath, "//div[@id='tasks']/div/a[text()='Javadoc']").click
  within_frame "classFrame" do
    page.should have_content content
  end
end

Then /^javadoc should display "([^"]*)" for default configuration$/ do |content|
  @job.last_build.wait_until_finished
  visit (@job.url + "/default")
  find(:xpath, "//div[@id='tasks']/div/a[text()='Javadoc']").click
  within_frame "classFrame" do
    page.should have_content content
  end
end
