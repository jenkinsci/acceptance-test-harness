require 'rubygems'

require 'fileutils'
require 'selenium-webdriver'
require 'socket'
require 'temp_dir'
require 'test/unit'

class JenkinsSeleniumTest < Test::Unit::TestCase
  TIMEOUT = 60
  JENKINS_DEBUG_LOG = Dir.pwd + "/last_test.log"

  def start_jenkins
    @tempdir = TempDir.create(:rootpath => Dir.pwd)
    @controlPort = 3002

    ENV["JENKINS_HOME"] = @tempdir
    jarfile = File.dirname(__FILE__) + "/../../../jenkins.war"
    @pipe = IO.popen("java -jar #{jarfile} --ajp13Port=-1 --controlPort=#{@controlPort} --httpPort=#{@httpPort} 2>&1")
    start = Time.now

    puts
    print "Bringing up a temporary Jenkins instance"
    Thread.new do
      while (line = @pipe.gets)
        @log.write(line)
        if line =~ /Jenkins is fully up and running/
          puts " Jenkins online and ready to be tested"
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

    while (!@ready && ((Time.now - start) < TIMEOUT))
      sleep 0.5
    end

    unless @ready
      raise "Could not bring up a Jenkins server"
    end
  end

  def stop_jenkins
    TCPSocket.open("localhost", @controlPort) do |sock|
      sock.write("0")
    end

    while @ready
      sleep 1
    end
  end

  def setup
    if File.exists? JENKINS_DEBUG_LOG
      FileUtils.rm JENKINS_DEBUG_LOG
    end

    # Chose a random port, just to be safe
    @httpPort = rand(65000 - 8080) + 8080

    @log = File.open(JENKINS_DEBUG_LOG, "w")
    @ready = false
    start_jenkins

    @base_url = "http://127.0.0.1:#{@httpPort}/"
    @driver = Selenium::WebDriver.for(:firefox)
    @waiter = Selenium::WebDriver::Wait.new(:timeout => 10)
  end

  def teardown
    stop_jenkins

    unless @log.nil?
      @log.close
    end
    unless @driver.nil?
      @driver.quit
    end

    FileUtils.rm_rf(@tempdir)

    unless @test_passed
      puts "It looks like the test failed/errored, so here's the console from Jenkins:"
      puts "--------------------------------------------------------------------------"
      File.open(JENKINS_DEBUG_LOG, 'r') do |fd|
        fd.each_line do |line|
          puts line
        end
      end
    end
  end

  def self.suite
    # This is a hack to prevent the accidental inclusion of an empty
    # "default_test" method in the test suite. The consequences of this extra
    # test method means we will bring Jenkins up and down one more time, which
    # sucks
    r = super
    r.tests.collect! do |t|
      unless t.method_name == "default_test"
        t
      end
    end
    r.tests.compact!
    r
  end
end
