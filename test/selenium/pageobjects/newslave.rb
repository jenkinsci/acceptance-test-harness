require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'

class NewSlave
  extend Test::Unit::Assertions

  def self.goto(driver, base_url)
    driver.navigate.to("#{base_url}/computer/new")
  end

  # TODO DRY, same NewJob, refactor
  def self.waiter
    Selenium::WebDriver::Wait.new(:timeout => 10)
  end
  
  def self.create_dumb(driver, base_url, name)
    self.goto(driver, base_url)

    self.waiter.until do
      driver.find_element(:id, "name")
    end

    name_field = driver.find_element(:id, "name")
    assert_not_nil name_field, "Couldn't find the Name input field on the new slave page"
    name_field.send_keys(name)

    job_type = driver.find_element(:name, "mode")
    assert_not_nil job_type, "Couldn't find slave mode radio button"
    job_type.click

    name_field.submit

    self.waiter.until do
      driver.title.match("Jenkins")
    end
  end


end
