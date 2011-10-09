require File.dirname(__FILE__) + "/../lib/base"
require File.dirname(__FILE__) + "/../pageobjects/newjob"
require File.dirname(__FILE__) + "/../pageobjects/job"
require File.dirname(__FILE__) + "/../pageobjects/pluginmanager"


class BuildTimeoutPluginTests < JenkinsSeleniumTest
  def setup
    super

    PluginManager.new(@driver, @base_url).install_plugin('build-timeout')
    restart_jenkins

    @job_name = "Selenium_Timeout_Test_Job"
    NewJob.create_freestyle(@driver, @base_url, @job_name)
    @job = Job.new(@driver, @base_url, @job_name)
  end

  def test_git_clone
    @job.configure do
      timeout_box = @driver.find_element(:xpath, "//input[@name='hudson-plugins-build_timeout-BuildTimeoutWrapper']")
      timeout_box.click

      timeout_value = @driver.find_element(:xpath, "//input[@name='build-timeout.timeoutMinutes']")
      timeout_value.send_keys "1"

      failBuild = @driver.find_element(:xpath, "//input[@name='build-timeout.failBuild']")
      failBuild.click

      @job.add_build_step "sleep 120"
    end

    @job.queue_build
    sleep 60

    build = @job.build(1)

    assert build.failed?, "The build should have failed!"
  end
end
