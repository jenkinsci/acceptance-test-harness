%w(local_controller log_watcher).each { |f| require File.dirname(__FILE__)+"/"+f }

# Runs Jenkins on Tomcat
#
# @attr [String] opts
#    specify the location of jenkins.war
# @attr [String] catalina_home
#    specify the location of Tomcat installation
class TomcatController < LocalJenkinsController
  register :tomcat

  def initialize(opts)
    super(opts)
    @catalina_home = opts[:catalina_home] || ENV['CATALINA_HOME'] || File.expand_path("./tomcat")
    raise "#@catalina_home doesn't exist, maybe you forgot to set CATALINA_HOME env var or provide catalina_home parameter? "  if !File.directory?(@catalina_home)
  end

  def start_process
    ENV["JENKINS_HOME"] = @tempdir
    FileUtils.rm_rf("#@catalina_home/webapps/jenkins") if Dir.exists?("#@catalina_home/webapps/jenkins")
    FileUtils.rm("#@catalina_home/webapps/jenkins.war") if File.exists?("#@catalina_home/webapps/jenkins.war")
    FileUtils.cp(@war,"#@catalina_home/webapps")
    @tomcat_log = "#@catalina_home/logs/catalina.out"
    FileUtils.rm @tomcat_log if File.exists?(@tomcat_log)

    @is_running = system("#@catalina_home/bin/startup.sh")
    raise "Cannot start Tomcat" if !@is_running

    IO.popen("tail -f #@tomcat_log")
  end

  def stop!
    puts
    print "    Stopping a temporary Jenkins/Tomcat instance\n"
    @is_running = !system("#@catalina_home/bin/shutdown.sh")
    raise "Cannot stop Tomcat" if @is_running
  end

  def url
    "http://127.0.0.1:8080/jenkins"
  end
end
