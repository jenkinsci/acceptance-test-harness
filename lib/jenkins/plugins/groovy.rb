#!/usr/bin/env ruby
require 'jenkins/pageobject'

module Plugins
  class Groovy < Jenkins::PageObject

    def self.add_auto_installation(name, version)
      find(:path, "/hudson-plugins-groovy-GroovyInstallation/tool/name").set(name)
      # by default Install automatically is checked
      find(:path, "/hudson-plugins-groovy-GroovyInstallation/tool/properties/hudson-tools-InstallSourceProperty/installers/id").click
      find(:xpath, "//option[@value='#{version}']").click
    end

    def self.add_local_installation(name, groovy_home)
      find(:path, "/hudson-plugins-groovy-GroovyInstallation/tool/name").set(name)
      # by default Install automatically is checked - need to uncheck
      find(:path, "/hudson-plugins-groovy-GroovyInstallation/tool/properties/hudson-tools-InstallSourceProperty").click 
      find(:path, "/hudson-plugins-groovy-GroovyInstallation/tool/home").set(groovy_home)
    end

    def prepare_autoinstall(runner)
      tempdir = runner.tempdir
      Dir.mkdir tempdir+'/updates'
      File.open("#{tempdir}/updates/hudson.plugins.groovy.GroovyInstaller", 'w') { |file| file.write('{"list": [{"id": "2.1.1", "name": "Groovy 2.1.1", "url": "http://dist.groovy.codehaus.org/distributions/groovy-binary-2.1.1.zip"}]}') }
    end


  end
end
