require File.dirname(__FILE__) + "/../lib/base"
require File.dirname(__FILE__) + "/../pageobjects/newjob"
require File.dirname(__FILE__) + "/../pageobjects/job"
require File.dirname(__FILE__) + "/../pageobjects/pluginmanager"


class GitPluginTests < JenkinsSeleniumTest
  def setup
    super

    PluginManager.new(@driver, @base_url).install_plugin('git')
    restart_jenkins

    @job_name = "Selenium_Git_Test_Job"
    NewJob.create_freestyle(@driver, @base_url, @job_name)
    @job = Job.new(@driver, @base_url, @job_name)
  end

  def test_git_clone
    @job.configure do
      label = @driver.find_element(:xpath, "//label[text()='Git']")
      label.click

      # XXX: This feels brittle as hell!
      url = @driver.find_element(:xpath, "//input[@name='_.url']")
      url.send_keys "git://github.com/jenkinsci/selenium-tests"

      @job.add_build_step "ls"
    end

    @job.queue_build

    build = @job.build(1)

    assert build.succeeded, "The build did not succeed!"

    assert_not_nil build.console.match("Cloning the remote Git repository")
  end
end
