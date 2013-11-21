Given /^a docker fixture "([^"]*)"$/ do |name|
  fixtures = File.dirname(__FILE__)+"/../../fixtures/#{name}"
  if !system("sudo docker build -t jenkins/#{name} .", :chdir=>fixtures)
    raise "Failed to build container"
  end
  spawn()
  IO.popen("sudo docker run -d -p 2222:22 jenkins/#{name}") do |p|
    @docker = p.gets
  end
  sleep 3
end

# this is for illustration only and we'll remove this
Then /^I login via ssh$/ do
  key = File.dirname(__FILE__)+"/../../fixtures/sshd/unsafe"
  if !system("ssh -p 2222 -i #{key} test@localhost uname -a")
    raise "ssh failed!"
  end
end

Before('@docker') do |scenario|
  if !system('which docker', :out => '/dev/null')
    puts 'Skipping: docker not available'
    scenario.skip!
  end
end

After('@docker') do |scenario|
  if @docker
    puts "Shutting down: #{@docker}"
    if !system("sudo docker kill #{@docker}", :out => '/dev/null')
      raise "Failed to kill #{@docker}"
    end
    if !system("sudo docker rm #{@docker}", :out => '/dev/null')
      raise "Failed to rm #{@docker}"
    end
  end
end