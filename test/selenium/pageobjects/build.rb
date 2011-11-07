require 'rubygems'
require 'selenium-webdriver'
require 'test/unit'
require 'rest_client'
require 'json'


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

  def json_rest_url
    build_url + "/api/json"
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
  
  def in_progress?
    response = RestClient.get(json_rest_url)
    json = JSON.parse(response.body)
    return json['building']
  end


end
