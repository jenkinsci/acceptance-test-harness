#!/usr/bin/env ruby

require 'jenkins/pageobject'
require 'jenkins/job'
require 'jenkins/build_step'

module Jenkins
  class Maven < Jenkins::PageObject

    def add_auto_installation(name, version)
      find(:path, "/hudson-tasks-Maven$MavenInstallation/tool/name").set(name)
      # by default Install automatically is checked
      select = find(:path, "/hudson-tasks-Maven$MavenInstallation/tool/properties/hudson-tools-InstallSourceProperty/installers/id")
      select.find(:xpath, "//option[@value='#{version}']").click
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

    def use_global_options(opts)
      find(:path, '/hudson-maven-MavenModuleSet/globalMavenOpts').set(opts)
    end
  end

  class MavenJob < Job

    register 'Maven', 'hudson.maven.MavenModuleSet'

    # override
    def build(number)
      Jenkins::MavenBuild.new(self, number)
    end

    def module(name)
      Jenkins::MavenModule.new(self, name)
    end

    def add_prebuild_shell_step(script)
      ensure_config_page

      find(:xpath, "//button[text()='Add pre-build step']").click
      find(:xpath, "//a[text()='Execute shell']").click
      find(:path, '/prebuilder/command').set(script)
    end

    def copy_resource(name, target)
      add_prebuild_shell_step "cp -r #{resource(name)} ./#{target}"
    end

    def step
      MavenJobBuildStep.new(self)
    end

    def options(options)
      open_advanced
      find(:path, '/mavenOpts').set(options)
    end

    private

    @advanced_open = false

    def open_advanced
      return if @advanced_open

      # Lets see how long will it take for this to blow up
      find(:path, "/advanced-button[1]").click
      @advanced_open = true
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
      "#{@job.url}/#{url_chunk}/"
    end

    def url_chunk
      @name.gsub(':', '$')
    end

    def open
      visit url
    end

    def json_api_url
      url + '/api/json'
    end

    def exists?
      begin
        json
      rescue JSON::ParserError
        return false
      end

      return true
    end
  end

  class MavenModuleBuild < PageObject

    def initialize(maven_module, maven_build)
      super(maven_build.base_url, "Maven module build")

      @module = maven_module
      @build = maven_build
    end

    def url
      "#{@module.job.url}/#{@build.number}/#{@module.url_chunk}/"
    end

    def json_api_url
      url + '/api/json'
    end

    def exists?
      begin
        json
      rescue JSON::ParserError
        return false
      end

      return true
    end
  end

  class MavenBuildStep < Jenkins::BuildStep

    register 'Maven', 'Invoke top-level Maven targets'

    def goals(goals)
      control("targets").set(goals)
    end

    def version(version)
      open_advanced
      select version
    end

    def properties(properties)
      open_advanced
      control('properties').set(properties)
    end

    def use_local_repo
      open_advanced
      control("usePrivateRepository").click
    end

    private

    @advanced_open = false

    def open_advanced
      return if @advanced_open

      control("advanced-button").click
      @advanced_open = true
    end
  end

  private
  # The only build step of Maven job
  class MavenJobBuildStep < Jenkins::MavenBuildStep

    def initialize(job)
      super(job, '')
    end

    def goals(goals)
      control("goals").set(goals)
    end

    def properties(properties)
      raise "There is no such thing in maven job"
    end
  end
end
