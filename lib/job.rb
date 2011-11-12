#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require File.dirname(__FILE__) + "/pageobject.rb"
require File.dirname(__FILE__) + "/build.rb"

module Jenkins
  class Job < PageObject
    def job_url
      @base_url + "/job/#{@name}"
    end

    def configure_url
      job_url + "/configure"
    end

    def add_script_step(script)
      ensure_config_page
      find(:xpath, "//button[text()='Add build step']").click
      find(:xpath, "//a[text()='Execute shell']").click
      find(:xpath, "//textarea[@name='command']").set(script)
    end

    def open
      visit(job_url)
    end

    def last_build
      return build("lastBuild") # Hacks!
    end

    def build(number)
      Jenkins::Build.new(@base_url, self, number)
    end

    def queue_build
      visit("#{job_url}/build?delay=0sec")
      # This is kind of silly, but I can't think of a better way to wait for the
      # build to complete
      sleep 5
    end

    def label_expression=(expression)
      ensure_config_page
      find(:xpath, "//input[@name='hasSlaveAffinity']").set(true)
      find(:xpath, "//input[@name='_.assignedLabelString']").set(expression)
    end

    def self.create_freestyle(base_url, name)
      visit("#{@base_url}/newJob")

      fill_in "name", :with => name
      find(:xpath, "//input[starts-with(@value, 'hudson.model.FreeStyleProject')]").set(true)
      click_button "OK"

      self.new(base_url, name)
    end
  end
end
