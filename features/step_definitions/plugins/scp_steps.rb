# scp plugin: https://wiki.jenkins-ci.org/display/JENKINS/SCP+plugin

When /^I configure docker fixture as SCP site$/ do
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    Plugins::SCP::GlobalConfig.add("localhost",@docker[:default].port(22),"/tmp","test","test","")
  end
end

When /^I publish "([^"]*)" with SCP plugin$/ do |files|
  step = @job.add_postbuild_step('Publish artifacts to SCP Repository')
  step.add(files, files)
end

When /^SCP plugin should have published "([^"]*)" on docker fixture$/ do |name|
  @docker[:default].cp "/tmp/#{name}","/tmp"
  # TODO: md5sum check?
end
