#!/usr/bin/env ruby

require 'jenkins/pageobject'
require 'jenkins/step'

module Jenkins
  # System configuration page
  class JenkinsConfig < PageObject
    include Jenkins::Step::Static

    def initialize(*args)
      super(*args)
    end

    def configure_url
      @base_url + "/configure"
    end

    def configure(&block)
      open
      unless block.nil?
        yield self
        save
      end
    end

    def open
      visit(configure_url)

      wait_for_cond do
        begin
          # wait until the config screen gets fully loaded
          !find(:css,"div.behavior-loading").visible?
        rescue Capybara::ElementNotFound
          # At least in Firefox, element with "display:none" results in element not found
          true
        end
      end
    end

    # set the number of executors on the master
    def executors=(v)
      find(:path, '/jenkins-model-MasterBuildConfiguration/numExecutors').set v.to_s
    end

    def add_tool_installer(title)
      click_button "Add #{title}"

      type = Jenkins::ToolInstaller.get title
      return type.new self
    end

    def add_tool(name)
      click_button(name)
    end

    def add_jdk_auto_installation(name, version)
      ensure_config_page
      find(:path, "/hudson-model-JDK/tool/name").set(name)
      # by default Install automatically is checked
      select = find(:path, "/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/id")
      select.find(:xpath, "//option[@value='#{version}']").click
      find(:path, "/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/acceptLicense").click
    end

    def enter_oracle_credentials(login, password)
      visit("/descriptorByName/hudson.tools.JDKInstaller/enterCredential")
      find(:xpath, "//input[@name='username']").set(login)
      find(:xpath, "//input[@name='password']").set(password)
      click_button("OK")
      click_button("Close")
    end

    def add_cloud(name)
      select_step name, find(:path, '/jenkins-model-GlobalCloudConfiguration/hetero-list-add[cloud]')
      # TODO: binding of fragments like BuildStep does
    end

    def mailer
      return Plugins::Mailer.new(self, '/hudson-tasks-Mailer')
    end

    def self.get(base_url, name)
      self.new(base_url, name)
    end
  end
end
