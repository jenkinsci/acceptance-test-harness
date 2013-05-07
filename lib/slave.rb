#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require 'rubygems'
require 'json'
require 'net/http'
require 'uri'

require File.dirname(__FILE__) + "/pageobject.rb"

module Jenkins
  class Slave < PageObject
    include Capybara::DSL
    extend Capybara::DSL

    def configure_url
      @base_url + "/computer/#{@name}/configure"
    end

    def json_api_url
      @base_url + "/computer/#{@name}/api/json"
    end

    def executors=(num_exec)
      find(:xpath, "//input[@name='_.numExecutors']").set(num_exec.to_s)
      # in my chrome, I need to move the focus out from the control to have it recognize the value entered
      # perhaps it's related to the way input type=number is emulated?
      find(:xpath, "//input[@name='_.remoteFS']").click
    end

    def remote_fs=(remote_fs)
      find(:xpath, "//input[@name='_.remoteFS']").set(remote_fs)
    end

    def labels=(labels)
      find(:xpath, "//input[@name='_.labelString']").set(labels)
    end

    def online?
      data = self.json
      return data != nil && !data["offline"]
    end

    def executor_count
      data = self.json
      return data["executors"].length
    end

    def self.dumb_slave(base_url, name)
      slave = self.new(base_url, name)
      visit("/computer/new")

      find(:xpath, "//input[@id='name']").set(slave.name)
      find(:xpath, "//input[@value='hudson.slaves.DumbSlave']").set(true)
      click_button "OK"
      # This form submission will drop us on the configure page

      # Just to make sure the dumb slave is set up properly, we should seed it
      # with a FS root and executors
      slave.executors = 1
      slave.remote_fs = "/tmp/#{slave.name}"

      jar="/tmp/slave#{Process.pid}.jar"

      # Configure this slave to be automatically launched from the master
      find(:xpath, "//option[@value='hudson.slaves.CommandLauncher']").select_option
      find(:xpath, "//input[@name='_.command']").set("sh -c 'curl -s -o #{jar} #{base_url}jnlpJars/slave.jar && java -jar #{jar}'")

      slave.save

      # Fire the slave up before we move on
      start = Time.now
      while (!slave.online? && (Time.now - start) < 60)
        sleep 1
      end

      return slave
    end
  end
end
