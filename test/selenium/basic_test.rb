require File.dirname(__FILE__) + "/lib/base"
require File.dirname(__FILE__) + "/pageobjects/newjob"


class BasicSanityTests < JenkinsSeleniumTest
  def test_proper_title
    @driver.navigate.to @base_url
    assert_not_nil @driver.title.match("\[Jenkins\]")
  end

  def test_create_new_job
    job_name = "Selenium_Test_Job"
    newjob = NewJob.new(@driver, @base_url)
    newjob.goto
    newjob.create_freestyle(job_name)
  end
end
