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

    def json_api_url
      "#{job_url}/api/json"
    end

    def config_xml
       job_url + "/config.xml"
    end

    def configure(&block)
      visit configure_url
      unless block.nil?
        yield
        save
      end
    end

    def add_scm(title)
      xpath = "//label[contains(text(),'#{title}')]/input[@name='scm']"
      element = find(:xpath, xpath)
      element.set(true)

      type = Jenkins::Scm.get title
      return type.new(self, element[:path])
    end

    def add_parameter(type)
      ensure_config_page

      return Jenkins::Parameter.add(self, type)
    end

    def add_shell_step(script)
      ensure_config_page

      find(:xpath, "//button[text()='Add build step']").locate.click
      find(:xpath, "//a[text()='Execute shell']").click
      find(:xpath, "//textarea[@name='command']").set(script)
    end

    def change_script_step(script)
      ensure_config_page

      find(:xpath, "//textarea[@name='command']").locate.set(script)
    end

    def add_postbuild_action(action)
      ensure_config_page

      find(:xpath, "//button[text()='Add post-build action']").locate.click
      find(:xpath, "//a[text()='#{action}']").click
    end

    def add_build_action(action)
      ensure_config_page

      find(:xpath, "//button[text()='Add build step']").click
      find(:xpath, "//a[text()='#{action}']").click
    end

    def open
      visit(job_url)
    end

    def open_config
      visit configure_url
    end

    def last_build
      return build("lastBuild") # Hacks!
    end

    def next_build_number
      return json['nextBuildNumber']
    end

    def workspace
      Jenkins::Workspace.new(job_url)
    end

    def build(number)
      Jenkins::Build.new(@base_url, self, number)
    end

    def configuration(name)
      Jenkins::Configuration.new(self, name)
    end

    def queue_build
      suffix = '/build?delay=0sec'
      visit job_url + suffix

      if !page.current_url.end_with?(suffix)
        # Build scheduled immediately
        last_build.wait_until_started
      else
        # We are waiting for parameters
      end
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
      find(:xpath, "//input[@name='hasSlaveAffinity']").locate.set(true)
      find(:xpath, "//input[@name='_.assignedLabelString']").set(expression)
    end

    def disable
      check 'disable'
    end

    def archive_artifacts(options)
      case
      when options[:includes]
        ensure_config_page
        find(:xpath, "//button[text()='Add post-build action']").locate.click
        begin
          find(:xpath, "//a[text()='Archive the artifacts']").click
        rescue Capybara::ElementNotFound
          # When cloudbees-jsync-archiver installed (pending JENKINS-17236):
          find(:xpath, "//a[text()='Archive artifacts (fast)']").click
        end
        find(:path, "/publisher/artifacts").set(options[:includes])
      when options[:excludes]
        find(:path, "/publisher/advanced-button").localte.click
        find(:path, "/publisher/excludes").set(options[:excludes])
      when options[:latestOnly]
        find(:path, "/publisher/advanced-button").locate.click
        find(:path, "/publisher/latestOnly").set(options[:latestOnly])
      end

    end

    def use_custom_workspace(workspace)
      ensure_config_page
      click_button "Advanced..."
      find(:path, "/customWorkspace").set(true)
      find(:path, "/customWorkspace/directory").set(workspace)
    end

    def copy_resource(resource, target)
      add_shell_step "cp -r #{File.dirname(__FILE__)}/../resources/#{resource} ./#{target}"
    end

    def self.create_freestyle(base_url, name)
      visit("#{@base_url}/newJob")

      fill_in "name", :with => name
      find(:xpath, "//input[starts-with(@value, 'hudson.model.FreeStyleProject')]").set(true)
      click_button "OK"

      self.new(base_url, name)
    end

    def self.copy_job(base_url, name, source_job_name)
      visit("#{@base_url}/newJob")
      fill_in "name", :with => name
      find(:xpath, "//input[@id='copy']").set(true)
      fill_in "from", :with => source_job_name
      click_button "OK"

      self.new(base_url, name)
    end

    def self.create(title, base_url)
      type = @@job_types[title];
      value = type.value
      name = Jenkins::PageObject.random_name

      visit "#{base_url}/newJob"
      fill_in "name", :with => name
      find(:xpath, "//input[starts-with(@value, '#{value}')]").set(true)
      click_button "OK"

      type.new(base_url, name)
    end

    @@job_types = Hash.new

    # Register job type
    def self.register(title, type)
      @@job_types[title] = type
    end

    # Get type by title
    def self.get(title)
      return @@job_types[title]
    end
  end
end
