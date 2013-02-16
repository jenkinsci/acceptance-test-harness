#!/usr/bin/env ruby

require File.dirname(__FILE__) + "/../pageobject.rb"

module Plugins
  class Ant < Jenkins::PageObject

    def self.add_ant_step(targets, ant_build_file)
      find_button('Add build step').locate.click
      click_link 'Invoke Ant'
      fill_in '_.targets', :with => targets
    end

    def self.add_auto_installation(name, version)
      find(:xpath, "//input[@path='/hudson-tasks-Ant$AntInstallation/tool/name']").set(name)
      # by default Install automatically is checked
      find(:xpath, "//select[@path='/hudson-tasks-Ant$AntInstallation/tool/properties/hudson-tools-InstallSourceProperty/installers/id']").click
      find(:xpath, "//option[@value='#{version}']").click
    end

    def self.add_local_installation(name, ant_home)
      find(:xpath, "//input[@path='/hudson-tasks-Ant$AntInstallation/tool/name']").set(name)
      # by default Install automatically is checked - need to uncheck
      find(:xpath, "//input[@path='/hudson-tasks-Ant$AntInstallation/tool/properties/hudson-tools-InstallSourceProperty']").click 
      find(:xpath, "//input[@path='/hudson-tasks-Ant$AntInstallation/tool/home']").set(ant_home)
    end

    def prepare_autoinstall(runner)
      tempdir = runner.tempdir
      Dir.mkdir tempdir+'/updates'
      File.open("#{tempdir}/updates/hudson.tasks.Ant.AntInstaller", 'w') { |file| file.write('{"list": [{"id": "1.8.4", "name": "1.8.4", "url": "http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.4-bin.zip"}]}') }
    end

  end
end
