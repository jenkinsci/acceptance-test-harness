require 'jenkins/build_step'
require 'jenkins/pageobject'

module Plugins
  class Ant < Jenkins::PageObject

    def self.add_auto_installation(name, version)
      find(:path, "/hudson-tasks-Ant$AntInstallation/tool/name").set(name)
      # by default Install automatically is checked
      find(:path, "/hudson-tasks-Ant$AntInstallation/tool/properties/hudson-tools-InstallSourceProperty/installers/id")
      find(:xpath, "//option[@value='#{version}']").click
    end

    def self.add_local_installation(name, ant_home)
      find(:path, "/hudson-tasks-Ant$AntInstallation/tool/name").set(name)
      # by default Install automatically is checked - need to uncheck
      find(:path, "/hudson-tasks-Ant$AntInstallation/tool/properties/hudson-tools-InstallSourceProperty").click
      find(:path, "/hudson-tasks-Ant$AntInstallation/tool/home").set(ant_home)
    end

    def prepare_autoinstall(runner)
      tempdir = runner.tempdir
      FileUtils.mkdir_p tempdir+'/updates'
      File.open("#{tempdir}/updates/hudson.tasks.Ant.AntInstaller", 'w') { |file| file.write('{"list": [{"id": "1.8.4", "name": "1.8.4", "url": "http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.4-bin.zip"}]}') }
    end
  end

  class AntStep < Jenkins::BuildStep

    register 'Ant', 'Invoke Ant'

    def target=(name)
      control('targets').set name
    end

    def version=(name)
      control('antName').select name
    end
  end
end
