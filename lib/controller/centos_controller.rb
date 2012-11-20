%w(vagrant_controller).each { |f| require File.dirname(__FILE__)+"/"+f }

class CentOSController < VagrantController
  register :centos

  def initialize(opts)
    super(opts)
    @repo_url = opts[:repo_url] || ENV['REPO_URL'] || 'http://pkg.jenkins-ci.org/redhat/jenkins.repo'
  end

  # start VM and install RPM package, but only for the first time
  def configure
    @vm.channel.system_ssh("sudo wget -O /etc/yum.repos.d/jenkins.repo #@repo_url")
    @vm.channel.system_ssh("sudo rpm --import http://pkg.jenkins-ci.org/redhat/jenkins-ci.org.key",:error_check=>false)
    @vm.channel.system_ssh("sudo yum -y install jenkins curl")
    @vm.channel.system_ssh("sudo service iptables stop")
    @vm.channel.system_ssh("sudo /etc/init.d/jenkins stop")
  end
end
