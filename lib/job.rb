#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require File.dirname(__FILE__) + "/pageobject.rb"
require File.dirname(__FILE__) + "/build.rb"

module Jenkins
  class Job < PageObject
    attr_accessor :timeout

    def initialize(*args)
      @timeout = 60 # Default all builds for this job to a 60s timeout
      super(*args)
    end

    def job_url
      @base_url + "/job/#{@name}"
    end

    def configure_url
      job_url + "/configure"
    end

    def configure(&block)
      visit configure_url
      unless block.nil?
        yield
        save
      end
    end

    def add_parameter(type,name,value)
      ensure_config_page
      find(:xpath, "//input[@name='parameterized']").set(true)
      find(:xpath, "//button[text()='Add Parameter']").click
      find(:xpath, "//a[text()='#{type}']").click
      find(:xpath, "//input[@name='parameter.name']").set(name)
      find(:xpath, "//input[@name='parameter.defaultValue']").set(value)
    end


    def add_script_step(script)
      ensure_config_page

      # HACK: on a sufficiently busy configuration page, the "add build step" button can end up below
      # the sticky "save" button, and Chrome driver says that's not clickable. So we first scroll all
      # the way down, so that "add build step" will appear top of the page.
      page.execute_script "window.scrollTo(0, document.body.scrollHeight)"

      find(:xpath, "//button[text()='Add build step']").click
      find(:xpath, "//a[text()='Execute shell']").click
      find(:xpath, "//textarea[@name='command']").set(script)
    end

    def add_ant_step(targets, ant_build_file)
      click_button 'Add build step'
      click_link 'Invoke Ant'
      fill_in '_.targets', :with => targets
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

    def wait_for_build(number)
      build = self.build(number)
      start = Time.now
      while (build.in_progress? && ((Time.now - start) < @timeout))
        sleep 1
      end
    end

    def label_expression=(expression)
      ensure_config_page
      find(:xpath, "//input[@name='hasSlaveAffinity']").set(true)
      find(:xpath, "//input[@name='_.assignedLabelString']").set(expression)
    end

    def disable
      check 'disable'
    end

    def self.create_freestyle(base_url, name)
      visit("#{@base_url}/newJob")

      fill_in "name", :with => name
      find(:xpath, "//input[starts-with(@value, 'hudson.model.FreeStyleProject')]").set(true)
      click_button "OK"

      self.new(base_url, name)
    end

    def self.create_matrix(base_url, name)
      visit("#{@base_url}/newJob")

      fill_in "name", :with => name
      find(:xpath, "//input[starts-with(@value, 'hudson.matrix.MatrixProject')]").set(true)
      click_button "OK"

      self.new(base_url, name)
    end
  end
end
