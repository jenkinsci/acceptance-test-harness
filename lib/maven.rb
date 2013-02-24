#!/usr/bin/env ruby

require File.dirname(__FILE__) + "/pageobject.rb"

module Jenkins
  class Maven < Jenkins::PageObject

    def add_maven_step(options)
      click_button 'Add build step'
      click_link 'Invoke top-level Maven targets'
      find(:xpath, "//input[@path='/builder/targets']").set(options[:goals])
      if(options[:version])
        find(:xpath, "//select[@name='maven.name']").click
        find(:xpath, "//option[@value='#{options[:version]}']").click
      end
    end

    def use_local_repo()
      find(:xpath, "//button[@path='/builder/advanced-button']").click
      find(:xpath, "//input[@path='/builder/usePrivateRepository']").click
    end

    def add_auto_installation(name, version)
      find(:xpath, "//input[@path='/hudson-tasks-Maven$MavenInstallation/tool/name']").set(name)
      # by default Install automatically is checked
      find(:xpath, "//select[@path='/hudson-tasks-Maven$MavenInstallation/tool/properties/hudson-tools-InstallSourceProperty/installers/id']").click
      find(:xpath, "//option[@value='#{version}']").click
    end

    def add_local_installation(name, maven_home)
      find(:xpath, "//input[@path='/hudson-tasks-Maven$MavenInstallation/tool/name']").set(name)
      # by default Install automatically is checked - need to uncheck
      find(:xpath, "//input[@path='/hudson-tasks-Maven$MavenInstallation/tool/properties/hudson-tools-InstallSourceProperty']").click 
      find(:xpath, "//input[@path='/hudson-tasks-Maven$MavenInstallation/tool/home']").set(maven_home)
    end

    def prepare_autoinstall(runner)
      tempdir = runner.tempdir
      Dir.mkdir tempdir+'/updates'
      File.open("#{tempdir}/updates/hudson.tasks.Maven.MavenInstaller", 'w') { |file| file.write('{"list": [{"id": "3.0.4", "name": "3.0.4", "url": "http://archive.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.zip"}]}') }
    end

  end
end
