require 'jenkins/pluginmanager'
require 'jenkins/job'

module Jenkins
  #
  # Top-level object that acts as an entry point to various systems
  #
  class JenkinsRoot
    attr_accessor :base_url

    # @param controller [JenkinsController]   encapsulates running Jenkins
    def initialize(controller)
      @controller = controller
      @base_url = controller.url
    end

    # @return [JenkinsController] encapsulates running Jenkins
    def controller
      @controller
    end

    # @return [Jenkins::PluginManager]
    def plugin_manager
      Jenkins::PluginManager.new(@base_url, nil)
    end

    # create a new job with a random name
    # @param title [String]   prefix of the display name that indicates the job type to be created.
    # @param name  [String]   the name of the newly created job. leave it to nil to assign a random value
    # @return [Jenkins::Job]
    def create_job(title, name=nil)
      Jenkins::Job.create title, @base_url, name
    end

    # Create a page object for the global configuration page
    # If a block is passed, it gets called in the context of the global config page object
    # @return [Jenkins::JenkinsConfig]
    def configure(&block)
      config = Jenkins::JenkinsConfig.new(@base_url, 'Jenkins global configuration')
      config.configure(&block) if block
      config
    end

    # @return [LogWatcher]
    def log
      @controller.log_watcher
    end
  end

  class RestartNeeded < Exception
  end
end
