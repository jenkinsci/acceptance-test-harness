%w(local_controller log_watcher).each { |f| require File.dirname(__FILE__)+"/"+f }

# Runs Jenkins on JBoss
#
# @attr [String] opts
#    specify the location of jenkins.war
# @attr [String] jboss_home
#    specify the location of JBoss installation
class JBossController < LocalJenkinsController
  register :jboss

  def initialize(opts)
    super(opts)

    @jboss_home = opts[:jboss_home] || ENV['JBOSS_HOME'] || File.expand_path("./jboss")
    raise "#@jboss_home doesn't exist, maybe you forgot to set JBOSS_HOME env var or provide jboss_home parameter? "  if !File.directory?(@jboss_home)
  end

  def start_process
    ENV["JENKINS_HOME"] = @tempdir
    FileUtils.rm("#@jboss_home/standalone/deployments/jenkins.war") if File.exists?("#@jboss_home/standalone/deployments/jenkins.war")
    FileUtils.rm("#@jboss_home/standalone/deployments/jenkins.war.deployed") if File.exists?("#@jboss_home/standalone/deployments/jenkins.war.deployed")
    FileUtils.rm("#@jboss_home/standalone/deployments/jenkins.war.failed") if File.exists?("#@jboss_home/standalone/deployments/jenkins.war.failed")

    FileUtils.cp(@war,"#@jboss_home/standalone/deployments")
    @jboss_log = "#@jboss_home/standalone/log/server.log"
    FileUtils.rm @jboss_log if File.exists?(@jboss_log)

    IO.popen("#@jboss_home/bin/standalone.sh")
  end

  def stop!
    system("#@jboss_home/bin/jboss-cli.sh --connect --command=:shutdown")
  end

  def url
    "http://127.0.0.1:8080/jenkins"
  end
end
