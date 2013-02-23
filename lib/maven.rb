#!/usr/bin/env ruby

require File.dirname(__FILE__) + "/pageobject.rb"

module Jenkins
  class Maven < Jenkins::PageObject

    def add_maven_step(version, goals)
      click_button 'Add build step'
      click_link 'Invoke top-level Maven targets'
      find(:xpath, "//input[@path='/builder/targets']").set(goals)
      find(:xpath, "//select[@name='maven.name']").click
      find(:xpath, "//option[@value='#{version}']").click
    end

    def add_auto_installation(name, version)
      find(:xpath, "//input[@path='/hudson-tasks-Maven$MavenInstallation/tool/name']").set(name)
      # by default Install automatically is checked
      find(:xpath, "//select[@path='/hudson-tasks-Maven$MavenInstallation/tool/properties/hudson-tools-InstallSourceProperty/installers/id']").click
      find(:xpath, "//option[@value='#{version}']").click
    end

    def prepare_autoinstall(runner)
      tempdir = runner.tempdir
      Dir.mkdir tempdir+'/updates'
      File.open("#{tempdir}/updates/hudson.tasks.Maven.MavenInstaller", 'w') { |file| file.write('{"list": [{"id": "3.0.4", "name": "3.0.4", "url": "http://archive.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.zip"}]}') }
    end

  end
end
