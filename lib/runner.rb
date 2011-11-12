require 'rubygems'

require 'fileutils'
require 'socket'
require 'temp_dir'
require 'test/unit'

module Jenkins
  class Runner
    TIMEOUT = 60
    JENKINS_DEBUG_LOG = Dir.pwd + "/last_test.log"
    attr_accessor :base_url

    def initialize
      @base_url = nil
    end

    def start_jenkins
      ENV["JENKINS_HOME"] = @tempdir
      jarfile = File.dirname(__FILE__) + "/../jenkins.war"
      @pipe = IO.popen(["java", "-jar", jarfile, "--ajp13Port=-1", "--controlPort=#{@controlPort}",
                          "--httpPort=#{@httpPort}",
                        :err => [:child, :out]])
      start = Time.now

      Thread.new do
        while (line = @pipe.gets)
          log_line(line)
          if line =~ /INFO: Jenkins is fully up and running/
            @ready = true
          end
        end
        @ready = false
      end

      while (!@ready && ((Time.now - start) < TIMEOUT))
        sleep 0.5
      end

      unless @ready
        raise "Could not bring up a Jenkins server"
      end
    end

    def stop_jenkins
      Process.kill("TERM", @pipe.pid)

      while @ready
        sleep 1
      end
    end

    def restart_jenkins
      stop_jenkins
      start_jenkins
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

    def setup
      if File.exists? JENKINS_DEBUG_LOG
        FileUtils.rm JENKINS_DEBUG_LOG
      end

      @tempdir = TempDir.create(:rootpath => Dir.pwd)
      @log_regex = nil
      @log_found = false

      # Chose a random port, just to be safe
      @httpPort = rand(65000 - 8080) + 8080
      @controlPort = 3002
      @base_url = "http://127.0.0.1:#{@httpPort}"

      @log = File.open(JENKINS_DEBUG_LOG, "w")
      @ready = false

      start_jenkins
    end

    def teardown(is_failure)
      stop_jenkins

      unless @log.nil?
        @log.close
      end

      if is_failure
        puts "It looks like the test failed/errored, so here's the console from Jenkins:"
        puts "--------------------------------------------------------------------------"
        File.open(JENKINS_DEBUG_LOG, 'r') do |fd|
          fd.each_line do |line|
            puts line
          end
        end
      end

      FileUtils.rm_rf(@tempdir)
    end
  end
end
