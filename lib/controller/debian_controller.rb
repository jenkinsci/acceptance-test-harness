%w(jenkins_controller).each { |f| require File.dirname(__FILE__)+"/"+f }

class DebianController < JenkinsController
  register :debian

  def initialize(opts)
    super(opts)
    @repo_url = opts[:repo_url] || ENV['REPO_URL'] || 'http://pkg.jenkins-ci.org/debian binary/'
  end

  # start VM and install debian package, but only for the first time
  def configure
    @vm.channel.system_ssh("wget -O - http://pkg.jenkins-ci.org/debian/jenkins-ci.org.key | sudo apt-key add -")
    @vm.channel.system_ssh("echo deb #@repo_url > /etc/apt/sources.list.d/jenkins.list", :sudo=>true)
    @vm.channel.system_ssh("sudo apt-get update")
    @vm.channel.system_ssh("sudo apt-get install -y jenkins")
    @vm.channel.system_ssh("sudo /etc/init.d/jenkins stop")
  end
end
