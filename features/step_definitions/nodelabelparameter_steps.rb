When /^I add label parameter "(.*?)"$/ do |param_name|
  param = @job.add_parameter 'Label'
  param.name = param_name
end

When /^I add node parameter "(.*?)"$/ do |param_name|
  @node_parameter = @job.add_parameter 'Node'
  @node_parameter.name = param_name
end

When /^I allow multiple nodes$/ do
  @node_parameter.allow_multiple
end
