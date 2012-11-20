%w(jenkins_controller).each { |f| require File.dirname(__FILE__)+"/"+f }

# abstract controller that puts Jenkins in a Linux via vagrant
class VagrantController < JenkinsController

  # start VM and install Jenkins
  def self.launch
    unless @launched
      @env = Vagrant::Environment.new({ :ui_class => Vagrant::UI::Colored,
                                        :cwd => "#{File.dirname(__FILE__)}/vagrant/#@type" })
      @env.load!

      # start the VM
      @env.cli("up")

      @vm = @env.vms[:default]

      configure   # to be provided by subtypes to actually install Jenkins

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
    @pipe = IO.popen("ssh -q -p 2222 -i ~/.vagrant.d/insecure_private_key vagrant@127.0.0.1 -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null sudo tail -F /var/log/jenkins/jenkins.log")
    @pid = @pipe.pid

    @log_watcher = LogWatcher.new(@pipe,@log)
    @log_watcher.wait_for_ready
  end

  def teardown
    unless @pid.nil?
      Process.kill 9,@pid
      @pid = nil
    end
    @log_watcher.close
    unless @log.nil?
      @log.close
    end
  end

  def stop!
    if @vm
      @vm.channel.system_ssh("sudo /etc/init.d/jenkins stop")
    end
  end

  def url
    "http://127.0.0.1:8080/"
  end
end
