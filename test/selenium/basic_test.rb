require File.dirname(__FILE__) + "/lib/base"
require File.dirname(__FILE__) + "/pageobjects/newjob"
require File.dirname(__FILE__) + "/pageobjects/pluginmanager"


class BasicSanityTests < JenkinsSeleniumTest
  def test_proper_title
    go_home
    assert_not_nil @driver.title.match("\[Jenkins\]")
  end

  def test_create_new_job
    job_name = "Selenium_Test_Job"
    NewJob.create_freestyle(@driver, @base_url, job_name)
    assert true
  end

  def test_install_git_plugin
    manager = PluginManager.new(@driver, @base_url)

    manager.install_plugin("git")

    restart_jenkins

    manager.assert_installed("git")
  end
end
