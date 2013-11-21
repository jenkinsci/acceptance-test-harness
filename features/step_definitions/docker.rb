Given /^a docker fixture "([^"]*)"$/ do |name|
  fixtures = File.dirname(__FILE__)+"/../../fixtures/#{name}"
  img = Jenkins::Docker.build(name)
  @docker = img.start([22])
end

# this is for illustration only and we'll remove this
Then /^I login via ssh$/ do
  key = File.dirname(__FILE__)+"/../../fixtures/sshd/unsafe"
  File.chmod(0600,key)

  if !system("ssh -p #{@docker.port(22)} -o StrictHostKeyChecking=no -i #{key} test@localhost uname -a")
    raise "ssh failed!"
  end
end

Before('@docker') do |scenario|
  if !Jenkins::Docker.available?
    puts 'Skipping: docker not available'
    scenario.skip!
  end
end

After('@docker') do |scenario|
  if @docker
    puts "Shutting down: #{@docker}"
    @docker.clean
  end
end