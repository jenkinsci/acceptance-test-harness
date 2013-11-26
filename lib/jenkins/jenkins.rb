require 'jenkins/pluginmanager'
require 'jenkins/job'

module Jenkins
  #
  # Top-level object that acts as an entry point to various systems
  #
  class JenkinsRoot
    attr_accessor :base_url

    def initialize(base_url)
      @base_url = base_url
    end

    # @return [Jenkins::PluginManager]
    def plugin_manager
      Jenkins::PluginManager.new(@base_url, nil)
    end

    # create a new job with a random name
    # @param title [String]   prefix of the display name that indicates the job type to be created.
    # @return [Jenkins::Job]
    def create_job(title)
      Jenkins::Job.create title, @base_url
    end
  end
end