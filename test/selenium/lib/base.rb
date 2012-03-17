require 'rubygems'

require 'fileutils'
require 'selenium-webdriver'
require 'socket'
require 'temp_dir'
require 'test/unit'

require File.dirname(__FILE__) + "/../pageobjects/globalconfig"

class JenkinsSeleniumTest < Test::Unit::TestCase
  TIMEOUT = 60
  JENKINS_DEBUG_LOG = Dir.pwd + "/last_test.log"
  JENKINS_LIB_DIR = Dir.pwd + "/lib"

  def start_jenkins
    ENV["JENKINS_HOME"] = @tempdir
    jarfile = ENV["JENKINS_WAR"] || File.dirname(__FILE__) + "/../../../jenkins.war"
    raise "jenkins.war doesn't exist in #{jarfile}" if !File.exists?(jarfile)
    @pipe = IO.popen("java -jar #{jarfile} --ajp13Port=-1 --controlPort=#{@controlPort} --httpPort=#{@httpPort} 2>&1")
    start = Time.now

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

    while (!@ready && ((Time.now - start) < TIMEOUT))
      sleep 0.5
    end

    unless @ready
      raise "Could not bring up a Jenkins server"
    end

    go_home
    # This hack-ish waiter is in place due to current versions of Jenkins LTS
    # not printing "Jenkins is fully up and running" to the logs once Jenkins
    # is up and running.
    #
    # This means LTS releases for now will drop the user on the "Jenkins is
    # getting ready to work" page, which means we have to poll until we hit the
    # proper "home page"
    Selenium::WebDriver::Wait.new(:timeout => 60, :interval => 1).until do
      # Due to a current bug with LTS which results in the "Jenkins is getting
      # ready to work" page redirecting to a gnarly exception page like this:
      #   <http://strongspace.com/rtyler/public/lts_startup_400.png>
      #
      # We're going to refresh the home page every single second >_<
      @driver.find_element(:xpath, "//a[@href='/view/All/newJob' and text()='New Job']") if @driver.navigate.refresh
    end
  end

  def stop_jenkins
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

  def go_home
    @driver.navigate.to @base_url
  end

  def setup
    if File.exists? JENKINS_DEBUG_LOG
      FileUtils.rm JENKINS_DEBUG_LOG
    end

    @tempdir = TempDir.create(:rootpath => Dir.pwd)
    @slave_tempdir = TempDir.create(:rootpath => Dir.pwd)
    @log_regex = nil
    @log_found = false

    # Chose a random port, just to be safe
    @httpPort = rand(65000 - 8080) + 8080
    @controlPort = 3002

    @log = File.open(JENKINS_DEBUG_LOG, "w")
    @ready = false

    @base_url = "http://127.0.0.1:#{@httpPort}/"
    @driver = Selenium::WebDriver.for(:firefox)
    @waiter = Selenium::WebDriver::Wait.new(:timeout => 10)

    start_jenkins
    GlobalConfig.instance.init(@driver,@base_url)
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
    FileUtils.rm_rf(@slave_tempdir)

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
