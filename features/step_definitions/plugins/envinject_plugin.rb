
When /^I prepare environment for the build by injecting variables$/ do |env_var|
  @job.open_config
  find(:path, "/properties/org-jenkinsci-plugins-envinject-EnvInjectJobProperty/on").set(true)
  find(:path, "/properties/org-jenkinsci-plugins-envinject-EnvInjectJobProperty/on/propertiesContent").set(env_var)
end

When /^I inject environment variables to the build$/ do |env_var|
  @job.open_config
  find(:path, "/org-jenkinsci-plugins-envinject-EnvInjectBuildWrapper").set(true)
  find(:path, "/org-jenkinsci-plugins-envinject-EnvInjectBuildWrapper/propertiesContent").set(env_var)
end

When /^I add build step injecting variables to the build$/ do |env_var|
  @job.open_config
  envinject_step = @job.add_build_step 'Env Inject'
  envinject_step.vars = env_var
end
