require 'rubygems'
require 'selenium-webdriver'

class NewJob
  def initialize(driver, base_url)
    @driver = driver
    @base_url = base_url
    @waiter = Selenium::WebDriver::Wait.new(:timeout => 10)
  end

  def goto
    @driver.navigate.to("#{@base_url}/newJob")
    assert_loaded
  end

  def assert_loaded
    @waiter.until do
      @driver.title.match("New Job")
    end
  end

  def create_freestyle(name)
    name_field = @driver.find_element(:id, "name")
    if name_field.nil?
      raise "Couldn't find the Job Name field?!"
    end

    name_field.send_keys(name)
    job_type = @driver.find_element(:xpath, "//input[@value='hudson.model.FreeStyleProject']")
    job_type.click
    name_field.submit

    created = nil
    @waiter.until do
      created = @driver.title.match("#{name} Config")
    end

    if created.nil?
      raise "It doesn't appear that we created a new job properly"
    end
  end

end
