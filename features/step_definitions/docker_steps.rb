Given /^a docker fixture "([^"]*)"$/ do |name|
  @docker ||= {}
  fixtures = File.dirname(__FILE__)+"/../../fixtures/#{name}"
  img = Jenkins::Docker.build(name)
  @docker[:default] = @docker[name] = img.start([22])
end

# this is for illustration only and we'll remove this
Then /^I can login via ssh( to fixture "([^"]*)")?$/ do |_,name|
  name ||= :default
  key = File.dirname(__FILE__)+"/../../fixtures/sshd/unsafe"
  File.chmod(0600,key)

  if !system("ssh -p #{@docker[name].port(22)} -o StrictHostKeyChecking=no -i #{key} test@localhost uname -a")
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
    @docker.each do |k,v|
      next if k==:default
      puts "Shutting down: #{v}"
      v.clean
    end
  end
end