require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'

class NewJob
  extend Test::Unit::Assertions

  def self.goto(driver, base_url)
    driver.navigate.to("#{base_url}/newJob")
  end

  def self.waiter
    Selenium::WebDriver::Wait.new(:timeout => 10)
  end

  def self.create_freestyle(driver, base_url, name)
    self.goto(driver, base_url)

    self.waiter.until do
      driver.find_element(:id, "name")
    end

    name_field = driver.find_element(:id, "name")
    assert_not_nil name_field, "Couldn't find the Name input field on the new job page"

    name_field.send_keys(name)
    job_type = driver.find_element(:xpath, "//input[starts-with(@value, 'hudson.model.FreeStyleProject')]")
    assert_not_nil job_type, "Couldn't find the Freestyle job type radio button"

    job_type.click
    name_field.submit

    self.waiter.until do
      driver.title.match("#{name} Config")
    end
  end

end
