require File.dirname(__FILE__) + "/lib/base"


class BasicSanityTests < JenkinsSeleniumTest
  def test_proper_title
    @driver.navigate.to @base_url
    assert_not_nil @driver.title.match("\[Jenkins\]")
  end

  def test_create_new_job
    job_name = "Selenium_Test_Job"

    @driver.navigate.to @base_url

    link = @driver.find_element(:link_text, "create new jobs")
    link.click

    name_field = nil
    @waiter.until do
      name_field = @driver.find_element(:id, "name")
    end

    assert_not_nil name_field, "Couldn't find the Job Name field?!"

    name_field.send_keys job_name

    job_type = @driver.find_element(:xpath, "//input[@value='hudson.model.FreeStyleProject']")
    job_type.click

    name_field.submit

    created = nil
    @waiter.until do
      created = @driver.title.match("#{job_name} Config")
    end

    assert_not_nil created, "It doesn't appear that we created a new job properly"
  end
end
