When /^I set the build timeout to (\d+) minutes$/ do |timeout|
  @buildTimeout = Plugins::BuildTimeout.new @job
  @buildTimeout.abortAfter timeout
end

When /^I set the build timeout to likely stuck$/ do
  @buildTimeout = Plugins::BuildTimeout.new @job
  @buildTimeout.abortWhenStuck
end

When /^I set abort build description$/ do
  @buildTimeout.useDescription
end
