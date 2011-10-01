require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'

class Job
  include Test::Unit::Assertions

  def initialize(driver, base_url, name)
    @driver = driver
    @base_url = base_url
    @name = name
  end

  def name
    @name
  end

  def job_url
    @base_url + "/job/#{@name}"
  end

  def configure_url
    job_url + "/configure"
  end

  def configure(&block)
    @driver.navigate.to(configure_url)

    unless block.nil?
      yield
      save
    end
  end

  def open
    @driver.navigate.to(job_url)
  end

  def disable
    assert_equal @driver.current_url, configure_url, "Cannot disableif I'm not on the configure page!"

    checkbox = @driver.find_element(:xpath, "//input[@name='disable']")
    assert_not_nil checkbox, "Couldn't find the disable button on the configuration page"
    checkbox.click
  end

  def save
    assert_equal @driver.current_url, configure_url, "Cannot save if I'm not on the configure page!"

    button = @driver.find_element(:xpath, "//button[text()='Save']")
    assert_not_nil button, "Couldn't find the Save button on the configuration page"
    button.click
  end
end
