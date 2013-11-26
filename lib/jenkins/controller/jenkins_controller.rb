require 'temp_dir'
require 'restclient'
require 'jenkins/pageobject'

# This module defines a contract that various Jenkins controller implementations need to support
#
# JenkinsController encapsulates a Jenkins installation, the subject of the test.
# It maybe a lone bare +jenkins.war+, it may be an installation of Jenkins on Tomcat,
# or it maybe a debian package installation of Jenkins that starts/stops via SysV init script.
# Jenkins may or may not be running on a local machine, etc.
#
# Each JenkinsController goes through the call sequence of +(start,restart*,stop)+ sprinkled with
# calls to +url+ and +diagnose+.
class JenkinsController

  # directory on the computer where this code is running that points to a directory
  # where test code can place log files, cache files, etc.
  # Note that this directory might not exist on the Jenkins master, since it can be
  # running on a separate computer.
  WORKSPACE = ENV['WORKSPACE'] || Dir.pwd

  JENKINS_DEBUG_LOG = WORKSPACE + "/last_test.log"

  attr_accessor :is_running, :log_watcher, :tempdir, :jenkins_version

  def initialize(*args)
    @is_running = false
    @log_watcher = nil

    FileUtils.rm JENKINS_DEBUG_LOG if File.exists? JENKINS_DEBUG_LOG
    @log = File.open(JENKINS_DEBUG_LOG, "w")
  end

  # download form-element-path.hpi and return its path
  def download_path_element

    source = 'http://updates.jenkins-ci.org/latest/form-element-path.hpi'

    target = WORKSPACE+'/path-element.hpi'

    # Download file only if missing
    if !File.exists? target

      open(target,'wb') do |f|
        rsp = RestClient.get source
        f.write(rsp.body)
      end
    end

    return target
  end

  # Starts Jenkins, with a brand new temporary JENKINS_HOME.
  #
  # This method can return as soon as the server becomes accessible via HTTP,
  # and it is the caller's responsibility to wait until Jenkins finishes its bootup sequence.
  def start!
    raise NotImplementedException
  end

  def start
    prestart

    @jenkins_version =  Jenkins::PageObject.new(url, '').jenkins_version
  end

  # Works like the start method, but don't use Capybara, which doesn't support concurrency
  #
  def prestart
    start! unless is_running?
    @is_running = true
  end

  # Restarts Jenkins
  def restart
    stop
    start
  end

  # Shuts down the Jenkins process.
  def stop!
    raise NotImplementedException
  end

  def stop
    stop! if is_running?
    @is_running = false
  end

  # return the URL where Jenkins is running, such as "http://localhost:9999/"
  # the URL must ends with '/'
  def url
    raise "Not implemented yet"
  end

  # local file path to obtain slave.jar
  # TODO: change this to URL.
  def slave_jar_path
    "#{@tempdir}/war/WEB-INF/slave.jar"
  end

  # called when a test failed. Produce diagnostic output to the console, to the extent you can,
  # such as printing out the server log, etc.
  def diagnose
    # default is no-op
  end

  # called at the very end to dispose any resources
  def teardown

  end

  # Wait for tool installer updates
  def wait_for_updates(name)

    pattern = /: Obtained the updated data file for .*#{Regexp.quote(name)}.*/i

    plugin_manager = Jenkins::PluginManager.new(url, nil)
    until @log_watcher.has_logged? pattern
      plugin_manager.check_for_updates

      sleep 1
    end
  end

  def is_running?
    @is_running
  end

  # registered implementations
  @@impls = {}

  # @return [JenkinsController]
  def self.create(args)
    t = @@impls[args[:type].to_sym]
    raise "Undefined controller type #{args[:type]}" if t.nil?
    t.new(args)
  end

  def self.register(type)
    @@impls[type] = self
    @type = type
  end

  def self.type
    @type
  end

  class RetryException < Exception
  end
end
