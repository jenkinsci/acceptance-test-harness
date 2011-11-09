require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'

require 'singleton'

class GlobalConfig
  include Test::Unit::Assertions
  include Singleton

  attr_reader :driver, :base_url

  def init(driver,base_url)
    @driver = driver
    @base_url = base_url
  end

  def config_url
    @base_url + "/configure"
  end

  def configure(&block)
    @driver.navigate.to(config_url)
    
    unless block.nil?
      yield
      save
    end
  end

  def add_ant_latest
    ensure_config_page
    button = @driver.find_element(:xpath, "//button[text()='Add Ant']")
    ensure_element(button, "Add Ant button")
    button.click    
    
    name = @driver.find_element(:xpath, "//input[@name='_.name']")
    ensure_element(name,"Ant name")
    name.send_keys "Latest"
  end

  def save
    ensure_config_page
    button = @driver.find_element(:xpath, "//button[text()='Save']")
    ensure_element(button, "Couldn't find the Save button on the configuration page")
    button.click
  end
  
  def ensure_config_page
    assert_equal @driver.current_url, config_url, "Cannot configure build steps if I'm not on the configure page"
  end

  def ensure_element(element,name)
    assert_not_nil element, "Couldn't find element '#{name}'"
  end

end
