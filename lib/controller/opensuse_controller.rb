%w(vagrant_controller).each { |f| require File.dirname(__FILE__)+"/"+f }

class OpenSUSEController < VagrantController
  register :opensuse

  def initialize(opts)
    super(opts)
    @repo_url = opts[:repo_url] || ENV['REPO_URL'] || 'http://pkg.jenkins-ci.org/opensuse/'
  end

  # start VM and install RPM package, but only for the first time
  def configure
    @vm.channel.system_ssh("sudo zypper addrepo #@repo_url jenkins")
    @vm.channel.system_ssh("sudo zypper -n --gpg-auto-import-keys install jenkins")
    @vm.channel.system_ssh("sudo /etc/init.d/jenkins stop")
  end
end
