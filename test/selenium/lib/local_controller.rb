require File.dirname(__FILE__) + "/jenkins_controller"

# Runs jenkins.war on the same system with built-in Winstone container
class LocalJenkinsController < JenkinsController
  TIMEOUT = 60
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
    @ready = false

    @base_url = "http://127.0.0.1:#{@httpPort}/"

    @log_regex = nil
    @log_found = false
  end

  def start
    ENV["JENKINS_HOME"] = @tempdir
    @pipe = IO.popen("java -jar #{@war} --ajp13Port=-1 --controlPort=#{@controlPort} --httpPort=#{@httpPort} 2>&1")
    start_time = Time.now

    puts
    print "Bringing up a temporary Jenkins instance"
    Thread.new do
      while (line = @pipe.gets)
        log_line(line)
        if line =~ /INFO: Completed initialization/
          puts " Jenkins completed initialization"
          @ready = true
        else
          unless @ready
            print '.'
            STDOUT.flush
          end
        end
      end
      @ready = false
    end

    while !@ready && ((Time.now - start_time) < TIMEOUT)
      sleep 0.5
    end

    unless @ready
      raise "Could not bring up a Jenkins server"
    end
  end

  def log_line(line)
    @log.write(line)
    @log.flush

    unless @log_regex.nil?
      if line.match(@log_regex)
        @log_found = true
      end
    end
  end

  def wait_until_logged(regex, timeout=60)
    start = Time.now.to_i
    @log_regex = regex

    while (Time.now.to_i - start) < timeout
      if @log_found
        @log_regex = nil
        @log_found = false
        return true
      end
      sleep 1
    end

    return false
  end

  def stop
    begin
      TCPSocket.open("localhost", @controlPort) do |sock|
        sock.write("0")
      end

      while @ready
        sleep 1
      end
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