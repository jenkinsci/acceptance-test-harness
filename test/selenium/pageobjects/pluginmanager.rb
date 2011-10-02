require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'

class PluginManager
  include Test::Unit::Assertions

  def initialize(driver, base_url)
    @driver = driver
    @base_url = base_url

    @updated = false
  end

  def waiter
    Selenium::WebDriver::Wait.new(:timeout => 10)
  end

  def url
    @base_url + "/pluginManager"
  end

  def open
    @driver.navigate.to url()
  end

  def check_for_updates
    @driver.navigate.to(url + "/checkUpdates")

    waiter.until do
      @driver.find_element(:xpath, "//span[@id='completionMarker' and text()='Done']")
    end

    @updated = true
    # This is totally arbitrary, it seems that the Available page doesn't
    # update properly if you don't sleep a bit
    sleep 5
  end

  def install_plugin(name)
    unless @updated
      check_for_updates
    end

    @driver.navigate.to(url + "/available")

    checkbox = @driver.find_element(:xpath, "//input[@name='plugin.#{name.downcase}.default']")
    assert_not_nil checkbox, "Couldn't find the plugin checkbox for the #{name} plugin"
    checkbox.click

    installbutton = @driver.find_element(:xpath, "//button[text()='Install']")
    assert_not_nil installbutton, "Couldn't find the install button on this page"
    installbutton.click


    # Give ourselves a good long time to find and install the plugin
    install_wait = Selenium::WebDriver::Wait.new(:timeout => 60)
    install_wait.until do
      @driver.navigate.to(url + "/installed")
      element = @driver.find_element(:xpath, "//div[@id='needRestart']")
      if element.displayed?
        element
      else
        nil
      end
    end
  end

  def assert_installed(name)
    @driver.navigate.to(url + "/installed")

    waiter.until do
      @driver.find_element(:xpath, "//input[@url='plugin/#{name.downcase}']")
    end

    assert true
  end
end
