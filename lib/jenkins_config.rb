#!/usr/bin/env ruby

require File.dirname(__FILE__) + "/pageobject.rb"

module Jenkins
  class JenkinsConfig < PageObject
    
    def initialize(*args)
      super(*args)
    end
    
    def configure_url
      @base_url + "configure"
    end

    def open
      visit(configure_url)
    end
    
    def add_tool(name)
      click_button(name)
    end

    def add_jdk_auto_installation(name, version)
      ensure_config_page
      find(:xpath, "//input[@path='/hudson-model-JDK/tool/name']").set(name)
      # by default Install automatically is checked
      find(:xpath, "//select[@path='/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/id']").click
      find(:xpath, "//option[@value='#{version}']").click
      find(:xpath, "//input[@path='/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/acceptLicense']").click
    end

    def enter_oracle_credentials(login, password)
      visit("/descriptorByName/hudson.tools.JDKInstaller/enterCredential")
      find(:xpath, "//input[@name='username']").set(login)
      find(:xpath, "//input[@name='password']").set(password)
      click_button("OK")
      click_button("Close")
    end

    # Get the version of Jenkins under test
    def jenkins_version()
      artifactId = 'org.jenkins-ci.main:jenkins-core'
      visit @base_url + 'about'

      text = wait_for("//*[starts-with(., '#{artifactId}:')]").text
      text.match("^#{artifactId}:(.*)$")[1]
    end

    def self.get(base_url, name)
      self.new(base_url, name)
    end

  end
end
