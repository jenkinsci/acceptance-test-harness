require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'

require File.dirname(__FILE__) + "/build"

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

  def queue_build
    @driver.navigate.to(job_url + "/build?delay=0sec")
    # This is kind of silly, but I can't think of a better way to wait for the
    # build to complete
    sleep 5
  end

  def queue_param_build
    build_button = @driver.find_element(:xpath, "//button[text()='Build']")
    ensure_element(build_button,"Param build button")
    build_button.click
  end

  def build(number)
    Build.new(@driver, @base_url, self, number)
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

  def add_parameter(type,name,value)
    ensure_config_page
    param_check_box = @driver.find_element(:name, "parameterized")
    ensure_element(param_check_box,"Parametrized build check box")
    param_check_box.click
    param_type_list = @driver.find_element(:xpath, "//button[text()='Add Parameter']")
    ensure_element(param_type_list,"Parameter type list")
    param_type_list.click
    param_type_link = @driver.find_element(:link,type)
    ensure_element(param_type_link,"Link to parameter fo type '#{type}'")
    param_type_link.click
    param_name = @driver.find_element(:xpath, "//input[@name='parameter.name']")
    ensure_element(param_name,"Parameter name")
    param_name.send_keys name
    param_def_value = @driver.find_element(:xpath, "//input[@name='parameter.defaultValue']")
    ensure_element(param_def_value,"Parameter default value")
    param_def_value.send_keys value
  end
   
  def wait_for_build
    #TODO improve it, some smarter approach
    @driver.navigate.refresh
    sleep 10
    @driver.navigate.refresh
  end

  def save
    assert_equal @driver.current_url, configure_url, "Cannot save if I'm not on the configure page!"

    button = @driver.find_element(:xpath, "//button[text()='Save']")
    assert_not_nil button, "Couldn't find the Save button on the configuration page"
    button.click
  end
  
  def ensure_config_page
    assert_equal @driver.current_url, configure_url, "Cannot configure build steps if I'm not on the configure page"
  end

  def ensure_element(element,name)
    assert_not_nil element, "Couldn't find element '#{name}'"
  end
    

end
