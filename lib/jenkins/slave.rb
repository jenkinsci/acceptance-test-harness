#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require 'rubygems'
require 'json'
require 'net/http'
require 'uri'

require 'jenkins/pageobject'

module Jenkins
  # Mix-in for page object that has a slave configuration UI
  module Slaveish
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

    # Set up this slave as a local slave that launches slave on the same host as Jenkins
    # call this in the context of the config UI
    def as_local
      jar="/tmp/slave#{Process.pid}.jar"

      # Configure this slave to be automatically launched from the master
      find(:xpath, "//option[@value='hudson.slaves.CommandLauncher']").select_option
      find(:xpath, "//input[@name='_.command']").set("sh -c 'curl -s -o #{jar} #{base_url}/jnlpJars/slave.jar && java -jar #{jar}'")
    end
  end

  class Slave < PageObject
    include Capybara::DSL
    extend Capybara::DSL
    include Slaveish

    def configure_url
      url + "configure"
    end

    def json_api_url
      url + "api/json"
    end

    def url
      "#{@base_url}/computer/#{name}/"
    end

    def build_history
      BuildHistory.new self
    end

    def online?
      data = self.json
      return data != nil && !data["offline"]
    end

    def executor_count
      data = self.json
      return data["executors"].length
    end

    def self.dumb_slave(base_url)
      return named_slave(base_url, self.random_name)
    end

    def self.named_slave(base_url, name)
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
      slave.as_local

      slave.save

      # Fire the slave up before we move on
      start = Time.now
      while (!slave.online? && (Time.now - start) < 60)
        sleep 1
      end

      return slave
    end
  end

  class Master < Slave

    def initialize(base_url)
      super(base_url, 'master')
    end

    def url
      "#{@base_url}/computer/(#{name})/"
    end
  end

  class BuildHistory < PageObject

    def initialize(node)
      super(node.base_url, 'Builds on ' + node.name)
      @node = node
    end

    def url
      @node.url + '/builds'
    end

    def include?(job_or_build)
      visit url
      page.should have_link_to job_or_build
    end

    private
    def have_link_to(node)
      have_xpath "//a[@href='#{relative_url(node.url)}/']"
    end

    def relative_url(url)
      url.gsub(@base_url, '')
    end
  end
end
