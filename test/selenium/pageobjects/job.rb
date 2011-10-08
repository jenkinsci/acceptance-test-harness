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

  def build
    @driver.navigate.to(job_url + "/build?delay=0sec")
  end

  def console_for_build(number)
    @driver.navigate.to(job_url + "/#{number}/console")

    console = @driver.find_element(:xpath, "//pre")
    assert_not_nil console, "Couldn't find the console text on the page"
    console.text
  end

  def disable
    assert_equal @driver.current_url, configure_url, "Cannot disableif I'm not on the configure page!"

    checkbox = @driver.find_element(:xpath, "//input[@name='disable']")
    assert_not_nil checkbox, "Couldn't find the disable button on the configuration page"
    checkbox.click
  end

  def add_build_step(script)
    assert_equal @driver.current_url, configure_url, "Cannot configure build steps if I'm not on the configure page"

    add_step = @driver.find_element(:xpath, "//button[text()='Add build step']")
    assert_not_nil add_step, "Couldn't find the 'Add build step' button"
    add_step.click

    exec_shell = @driver.find_element(:xpath, "//a[text()='Execute shell']")
    assert_not_nil exec_shell, "Couldn't find the 'Execute shell' link"
    exec_shell.click

    # We need to give the textarea a little bit of time to show up, since the
    # JavaScript doesn't seem to make it appear "immediately" as far as the web
    # driver is concerned
    textarea = nil
    Selenium::WebDriver::Wait.new(:timeout => 10).until do
      textarea = @driver.find_element(:xpath, "//textarea[@name='command']")
      textarea
    end

    assert_not_nil textarea, "Couldn't find the command textarea on the page"
    textarea.send_keys script
  end

  def save
    assert_equal @driver.current_url, configure_url, "Cannot save if I'm not on the configure page!"

    button = @driver.find_element(:xpath, "//button[text()='Save']")
    assert_not_nil button, "Couldn't find the Save button on the configuration page"
    button.click
  end
end
