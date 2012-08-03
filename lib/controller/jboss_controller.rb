%w(jenkins_controller log_watcher).each { |f| require File.dirname(__FILE__)+"/"+f }

# Runs Jenkins on JBoss
#
# @attr [String] opts
#    specify the location of jenkins.war
# @attr [String] jboss_home
#    specify the location of JBoss installation 
class JBossController < JenkinsController
  register :jboss
  JENKINS_DEBUG_LOG = Dir.pwd + "/last_test.log"

  def initialize(opts)
    @war = opts[:war] || ENV['JENKINS_WAR'] || File.expand_path("./jenkins.war")
    raise "jenkins.war doesn't exist in #{@war}, maybe you forgot to set JENKINS_WAR env var? "  if !File.exists?(@war)

    @jboss_home = opts[:jboss_home] || ENV['JBOSS_HOME'] || File.expand_path("./jboss")
    raise "#{@jboss_home} doesn't exist, maybe you forgot to set JBOSS_HOME env var or provide jboss_home parameter? "  if !File.directory?(@jboss_home)

    @tempdir = TempDir.create(:rootpath => Dir.pwd)
    
    FileUtils.rm JENKINS_DEBUG_LOG if File.exists? JENKINS_DEBUG_LOG
    @log = File.open(JENKINS_DEBUG_LOG, "w")

    @base_url = "http://127.0.0.1:8080/jenkins/"
  end

  def start!
    ENV["JENKINS_HOME"] = @tempdir
    puts
    print "    Bringing up a temporary Jenkins/JBoss instance\n"

    FileUtils.rm("#{@jboss_home}/standalone/deployments/jenkins.war") if File.exists?("#{@jboss_home}/standalone/deployments/jenkins.war")
    FileUtils.rm("#{@jboss_home}/standalone/deployments/jenkins.war.deployed") if File.exists?("#{@jboss_home}/standalone/deployments/jenkins.war.deployed")
    FileUtils.rm("#{@jboss_home}/standalone/deployments/jenkins.war.failed") if File.exists?("#{@jboss_home}/standalone/deployments/jenkins.war.failed")

    FileUtils.cp(@war,"#{@jboss_home}/standalone/deployments")
    @jboss_log = "#{@jboss_home}/standalone/log/server.log" 
    FileUtils.rm @jboss_log if File.exists?(@jboss_log)

    @pipe = IO.popen("#{@jboss_home}/bin/standalone.sh")
    @pid = @pipe.pid
    @is_running = true
   
    @log_watcher = LogWatcher.new(@pipe,@log,/jenkins.*Completed initialization/)
    @log_watcher.wait_for_ready
  end

  def stop!
    system("#{@jboss_home}/bin/jboss-cli.sh --connect --command=:shutdown")
    @is_running = false
  end

  def teardown
    unless @log.nil?
      @log.close
    end
    FileUtils.rm_rf(@tempdir)
  end

  def url
    @base_url
  end

  def diagnose
    puts "It looks like the test failed/errored, so here's the console from Jenkins:"
    puts "--------------------------------------------------------------------------"
    File.open(JENKINS_DEBUG_LOG, 'r') do |fd|
      fd.each_line do |line|
        puts line
      end
    end
  end
end
