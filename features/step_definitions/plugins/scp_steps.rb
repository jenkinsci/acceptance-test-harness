# scp plugin: https://wiki.jenkins-ci.org/display/JENKINS/SCP+plugin

When /^I configure docker fixture as SCP site$/ do
  @jenkins_config = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')
  @jenkins_config.configure do
    Plugins::SCP::GlobalConfig.add("localhost",@docker[:default].port(22),"/tmp","test","test","")
  end
end

When /^I publish "([^"]*)" with SCP plugin$/ do |files|
  section = find(:xpath, "//div[@descriptorId='be.certipost.hudson.plugin.SCPRepositoryPublisher']")
  section.find(:path, '/publisher/repeatable-add').click()
  section.find(:path, '/publisher/entries/sourceFile').set(files)
  section.find(:path, '/publisher/entries/filePath').set(files)
end

When /^SCP plugin should have published "([^"]*)" on docker fixture$/ do |name|
  @docker[:default].cp "/tmp/#{name}","/tmp"
  # TODO: md5sum check?
end