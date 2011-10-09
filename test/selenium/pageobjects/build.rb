require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'

class Build
  include Test::Unit::Assertions

  def initialize(driver, base_url, job, number)
    @driver = driver
    @base_url = base_url
    @job = job
    @number = number
  end

  def job
    @job
  end

  def number
    @number
  end

  def build_url
    @job.job_url + "/#{@number}"
  end

  def console
    @console ||= begin
      @driver.navigate.to(build_url + "/console")

      console = @driver.find_element(:xpath, "//pre")
      assert_not_nil console, "Couldn't find the console text on the page"
      console.text
    end
  end

  def succeeded?
    @succeeded ||= begin
      @driver.navigate.to(build_url)
      status_icon = @driver.find_element(:xpath, "//img[@src='buildStatus']")

      status_icon["tooltip"] == "Success"
    end
  end

  def failed?
    return !succeeded
  end

end
