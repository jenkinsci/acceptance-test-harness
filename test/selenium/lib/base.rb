require 'rubygems'

require 'fileutils'
require 'selenium-webdriver'
require 'socket'
require 'temp_dir'
require 'test/unit'

require File.dirname(__FILE__) + "/local_controller"
require File.dirname(__FILE__) + "/sysv_init_controller"
require File.dirname(__FILE__) + "/../pageobjects/globalconfig"

class JenkinsSeleniumTest < Test::Unit::TestCase
  TIMEOUT = 60

  # TODO: get rid of this by downloading slave.jar
  JENKINS_LIB_DIR = Dir.pwd + "/lib"

  # @return [JenkinsController]
  attr_reader :controller

  # default is to run locally
  @@controller_args = { :type => :local }

  # set the parameters for creating controller
  def self.controller_args=(hash)
    @@controller_args = hash
  end

  def start_jenkins
    @controller.start
    @base_url = @controller.url

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
    @controller.stop
  end


  def restart_jenkins
    @controller.restart
  end

  def go_home
    @driver.navigate.to @base_url
  end

  def setup
    @controller = JenkinsController.create @@controller_args
    @slave_tempdir = TempDir.create(:rootpath => Dir.pwd)

    @driver = Selenium::WebDriver.for(:firefox)
    @waiter = Selenium::WebDriver::Wait.new(:timeout => 10)

    start_jenkins
    GlobalConfig.instance.init(@driver,@base_url)
  end

  def teardown
    stop_jenkins

    unless @driver.nil?
      @driver.quit
    end

    @controller.teardown
    FileUtils.rm_rf(@slave_tempdir)

    unless @test_passed
      @controller.diagnose
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
