#!/usr/bin/env ruby

require File.dirname(__FILE__) + "/pageobject.rb"
require File.dirname(__FILE__) + "/job.rb"
require File.dirname(__FILE__) + "/build.rb"

module Jenkins
  class Maven < Jenkins::PageObject

    def add_maven_step(options)
      click_button 'Add build step'
      click_link 'Invoke top-level Maven targets'
      find(:path, "/builder/targets").set(options[:goals])
      if(options[:version])
        find(:xpath, "//select[@name='maven.name']").click
        find(:xpath, "//option[@value='#{options[:version]}']").click
      end
    end

    def use_local_repo
      find(:path, "/builder/advanced-button").click
      find(:path, "/builder/usePrivateRepository").locate.click
    end

    def add_auto_installation(name, version)
      find(:path, "/hudson-tasks-Maven$MavenInstallation/tool/name").set(name)
      # by default Install automatically is checked
      find(:path, "/hudson-tasks-Maven$MavenInstallation/tool/properties/hudson-tools-InstallSourceProperty/installers/id").click
      find(:xpath, "//option[@value='#{version}']").click
    end

    def add_local_installation(name, maven_home)
      find(:path, "/hudson-tasks-Maven$MavenInstallation/tool/name").set(name)
      # by default Install automatically is checked - need to uncheck
      find(:path, "/hudson-tasks-Maven$MavenInstallation/tool/properties/hudson-tools-InstallSourceProperty").click
      find(:path, "/hudson-tasks-Maven$MavenInstallation/tool/home").set(maven_home)
    end

    def prepare_autoinstall(runner)
      tempdir = runner.tempdir
      Dir.mkdir tempdir+'/updates'
      File.open("#{tempdir}/updates/hudson.tasks.Maven.MavenInstaller", 'w') { |file| file.write('{"list": [{"id": "3.0.4", "name": "3.0.4", "url": "http://archive.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.zip"}]}') }
    end
  end

  class MavenJob < Job

    def self.value
      return 'hudson.maven.MavenModuleSet'
    end

    # override
    def build(number)
      Jenkins::MavenBuild.new(self, number)
    end

    def module(name)
      Jenkins::MavenModule.new(self, name)
    end

    def add_prebuild_shell_step(script)
      ensure_config_page

      find(:xpath, "//button[text()='Add pre-build step']").locate.click
      find(:xpath, "//a[text()='Execute shell']").click
      find(:path, '/prebuilder/command').set(script)
    end

    def copy_resource(resource, target)
      add_prebuild_shell_step "cp -r #{File.dirname(__FILE__)}/../resources/#{resource} ./#{target}"
    end

    def maven_goals(goals)
      find(:path, "/goals").set(goals)
    end

    def maven_version(version)
      open_advanced
      find(:xpath, "//select[@name='maven_version']").click
      find(:xpath, "//option[@value='#{options[:version]}']").click
    end

    def use_local_repo
      open_advanced
      find(:path, "/usePrivateRepository").click
    end

    private

    @advanced_open = false

    def open_advanced
      return if advanced_open

      # Lets see how long will it take for this to blow up
      find(:path, "/advanced-button[1]").click
      advanced_open = true
    end
  end

  class MavenBuild < Build

    def initialize(job, number)
      super(job.base_url, job, number)
    end

    def module(name)
      Jenkins::MavenModuleBuild.new(Jenkins::MavenModule.new(job, name), self)
    end
  end

  class MavenModule < PageObject

    attr_accessor :job;

    def initialize(job, name)
      super(job.base_url, nil)

      @job = job
      @name = name
    end

    def build(number)
      Jenkins::MavenModuleBuild.new(self, Jenkins::MavenBuild.new(job, number))
    end

    def url
      "#{@job.job_url}/#{@name}"
    end
  end

  class MavenModuleBuild < PageObject

    def initialize(maven_module, maven_build)
      super(maven_build.base_url, "Maven module build")

      @module = maven_module
      @build = maven_build
    end

    def url
      "#{@module.url}/#{@build.number}"
    end
  end
end

Jenkins::Job.register('Maven', Jenkins::MavenJob)
