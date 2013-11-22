require 'fixtures'

Given /^a docker fixture "([^"]*)"$/ do |name|
  @docker ||= {}
  @docker[:default] = @docker[name] = Jenkins::Fixtures::Fixture.start(name)
end

# this is for illustration only and we'll remove this
Then /^I can login via ssh( to fixture "([^"]*)")?$/ do |_,name|
  name ||= :default
  @docker[name].ssh_with_publickey()
end

Before('@docker') do |scenario|
  if !Jenkins::Docker.available?
    puts 'Skipping: docker not available'
    scenario.skip!
  end
end

After('@docker') do |scenario|
  if @docker
    @docker.each do |k,v|
      next if k==:default
      puts "Shutting down: #{v}"
      v.clean
    end
  end
end