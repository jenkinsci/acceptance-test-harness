%w(jenkins_controller).each { |f| require File.dirname(__FILE__)+"/"+f }

class DebianController < JenkinsController
  register :debian

  def initialize(opts)
    super(opts)

    @repo_url = opts[:repo_url] || ENV['REPO_URL'] || 'http://pkg.jenkins-ci.org/debian binary/'
  end

  # start VM and install debian package, but only for the first time
  def self.launch
    unless @launched
      @env = Vagrant::Environment.new({ :ui_class => Vagrant::UI::Colored,
                                    :cwd => "#{File.dirname(__FILE__)}/vagrant/debian" })
      @env.load!

      # start the VM
      @env.cli("up")

      @vm = @env.vms[:default]

      @vm.channel.system_ssh("wget -O - http://pkg.jenkins-ci.org/debian/jenkins-ci.org.key | sudo apt-key add -")
      @vm.channel.system_ssh("echo deb http://pkg.jenkins-ci.org/debian binary/ > /etc/apt/sources.list.d/jenkins.list", :sudo=>true)
      @vm.channel.system_ssh("sudo apt-get update")
      @vm.channel.system_ssh("sudo apt-get install -y jenkins")
      @vm.channel.system_ssh("sudo /etc/init.d/jenkins stop")

      @launched = true
    end
    return @vm
  end

  def self.wipe
    # clean up JENKINS_HOME
    @vm.channel.system_ssh("sudo rm -rf /var/lib/jenkins/*")
  end

  def start!
    @vm = self.class.launch

    puts
    print "    Bringing up a temporary Jenkins instance"

    @vm.channel.system_ssh("sudo kill -9 $(pgrep java) > /dev/null 2>&1", :error_check=>false)
    self.class.wipe
    @vm.channel.system_ssh("sudo /etc/init.d/jenkins start")
    # TODO: how do I turn it into a pipe? maybe by calling ssh?
    @pipe = IO.popen("ssh -p 2222 -i ~/.vagrant.d/insecure_private_key vagrant@127.0.0.1 -o StrictHostKeyChecking=no tail -F /var/log/jenkins/jenkins.log")
    @pid = @pipe.pid

    @log_watcher = LogWatcher.new(@pipe,@log)
    @log_watcher.wait_for_ready
  end

  def teardown
    unless @log.nil?
      @log.close
    end
    Process.kill(9,@pid)
  end

  def stop!
    if @vm
      @vm.channel.system_ssh("sudo /etc/init.d/jenkins start")
    end
  end

  def url
    "http://127.0.0.1:8080/"
  end
end
