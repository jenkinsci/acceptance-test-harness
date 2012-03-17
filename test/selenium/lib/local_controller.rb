%w(jenkins_controller log_watcher).each { |f| require File.dirname(__FILE__)+"/"+f }

# Runs jenkins.war on the same system with built-in Winstone container
class LocalJenkinsController < JenkinsController
  JENKINS_DEBUG_LOG = Dir.pwd + "/last_test.log"
  register :local

  # @param [Hash] args
  #     :war  => specify the location of jenkins.war
  def initialize(args)
    @war = args[:war] || ENV['JENKINS_WAR'] || File.dirname(__FILE__) + "/../../../jenkins.war"
    raise "jenkins.war doesn't exist in #{@war}, maybe you forgot to set JENKINS_WAR env var?" if !File.exists?(@war)

    @tempdir = TempDir.create(:rootpath => Dir.pwd)

    # Chose a random port, just to be safe
    @httpPort    = rand(65000 - 8080) + 8080
    @controlPort = rand(65000 - 8080) + 8080

    FileUtils.rm JENKINS_DEBUG_LOG if File.exists? JENKINS_DEBUG_LOG
    @log = File.open(JENKINS_DEBUG_LOG, "w")

    @base_url = "http://127.0.0.1:#{@httpPort}/"
  end

  def start
    ENV["JENKINS_HOME"] = @tempdir
    puts
    print "Bringing up a temporary Jenkins instance"
    @pipe = IO.popen("java -jar #{@war} --ajp13Port=-1 --controlPort=#{@controlPort} --httpPort=#{@httpPort} 2>&1")

    @log_watcher = LogWatcher.new(@pipe,@log)
    @log_watcher.wait_for_ready
  end

  def stop
    begin
      TCPSocket.open("localhost", @controlPort) do |sock|
        sock.write("0")
      end

      @log_watcher.wait_for_ready false
    rescue => e
      puts "Failed to cleanly shutdown Jenkins #{e}"
      puts "  "+e.backtrace.join("\n  ")
    end
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