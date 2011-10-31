require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'
require 'rest_client'
require 'json'

require File.dirname(__FILE__) + "/../pageobjects/newjob"

class Slave
  include Test::Unit::Assertions

  def initialize(driver, base_url, name)
    @driver = driver
    @base_url = base_url
    @name = name
  end

  def configure_url
    @base_url + "/computer/#{@name}/configure"
  end

  def json_rest_url
    @base_url + "/computer/#{@name}/api/json"
  end

  def set_num_executors(num_exec)
    #ensure_config_page
    param_name = @driver.find_element(:xpath, "//input[@name='_.numExecutors']")
    ensure_element(param_name,"# of executors")
    param_name.send_keys num_exec
  end

  def set_remote_fs(remote_fs)
    #ensure_config_page
    param_name = @driver.find_element(:xpath, "//input[@name='_.remoteFS']")
    ensure_element(param_name,"Remote FS root")
    param_name.send_keys remote_fs
  end
  
  def set_labels(labels)
    #ensure_config_page
    param_name = @driver.find_element(:xpath, "//input[@name='_.labelString']")
    ensure_element(param_name,"Labels")
    param_name.send_keys labels
  end

  def set_command_on_master(launch_command)
    method = @driver.find_element(:css,"select.setting-input.dropdownList")
    #TODO cannot find any select equivalent in Ruby API
    method.click
    method.send_keys :arrow_down
    method.send_keys :arrow_down
    method.click

    #TODO need to move focus somewhere else to command line appers, probebly there is some better way how to do it
    usage = @driver.find_element(:xpath,"//select[@name='mode']")
    usage.click
    usage.click

    command = @driver.find_element(:xpath,"//input[@name='_.command']")
    command.send_keys launch_command
  end

  def save
    button = @driver.find_element(:xpath, "//button[text()='Save']")
    assert_not_nil button, "Couldn't find the Save button on the configuration page"
    button.click
  end



  def is_offline
    response = RestClient.get(json_rest_url)
    js = JSON.parse(response.body)
    return js['offline']
  end


  def ensure_config_page
    assert_equal @driver.current_url, configure_url, "Cannot configure build steps if I'm not on the configure page"
  end

  def ensure_element(element,name)
    assert_not_nil element, "Couldn't find element '#{name}'"
  end


end
