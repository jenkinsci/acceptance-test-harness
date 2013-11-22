#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require 'build'

module Jenkins
  class Job < PageObject
    attr_accessor :timeout

    def initialize(*args)
      @timeout = 60 # Default all builds for this job to a 60s timeout
      super(*args)
    end

    def url
      @base_url + "/job/#{@name}"
    end


    def json_api_url
      "#{@base_url}/job/#{@name}/api/json"
    end

    def configure_url
      url + "/configure"
    end

    def json_api_url
      "#{url}/api/json"
    end

    def config_xml
       url + "/config.xml"
    end

    def configure(&block)
      visit configure_url
      unless block.nil?
        yield
        save
      end
    end

    def add_scm(type)
      ensure_config_page

      return Jenkins::Scm.add(self, type)
    end

    def add_parameter(type)
      ensure_config_page

      return Jenkins::Parameter.add(self, type)
    end

    def add_build_step(type)
      ensure_config_page

      return Jenkins::BuildStep.add(self, type)
    end

    def add_shell_step(script)
      step = add_build_step 'Shell'
      step.command script
      return step
    end

    def add_postbuild_step(type)
      ensure_config_page

      return Jenkins::PostBuildStep.add(self, type)
    end

    def add_postbuild_action(action)
      ensure_config_page

      find(:xpath, "//button[text()='Add post-build action']").click
      find(:xpath, "//a[text()='#{action}']").click
    end

    def open
      visit(url)
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
      Jenkins::Workspace.new(url)
    end

    def build(number)
      Jenkins::Build.new(@base_url, self, number)
    end

    def queue_build
      nb = json["nextBuildNumber"]

      suffix = '/build?delay=0sec'
      visit url + suffix

      if !page.has_button?('Build')
        # Build scheduled immediately
        # wait for the build to start before we go back
        build(nb).wait_until_started
      else
        # We are waiting for parameters
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

    def use_custom_workspace(workspace)
      ensure_config_page
      click_button "Advanced..."
      find(:path, "/customWorkspace").set(true)
      find(:path, "/customWorkspace/directory").set(workspace)
    end

    def copy_resource(resource, target)
      add_shell_step "cp -r #{File.dirname(__FILE__)}/../resources/#{resource} ./#{target}"
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
      create_named title, base_url, Jenkins::PageObject.random_name
    end

    def self.create_named(title, base_url, name)
      sut_type = @@job_types[title][:sut];
      page_object_type = @@job_types[title][:page_object];

      visit "#{base_url}/newJob"
      fill_in "name", :with => name
      find(:xpath, "//input[starts-with(@value, '#{sut_type}')]").set(true)
      click_button "OK"

      page_object_type.new(base_url, name)
    end

    @@job_types = Hash.new

    # Register job type
    def self.register(title, type)
      @@job_types[title] = { page_object: self, sut: type }
    end

    # Get type by title
    def self.get(title)
      return @@job_types[title]
    end

    register 'FreeStyle', 'hudson.model.FreeStyleProject'
  end
end
