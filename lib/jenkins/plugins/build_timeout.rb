require 'jenkins/pagearea'

module Plugins
  class BuildTimeout
    include Jenkins::PageArea

    def initialize(job)
      @job = job
    end

    def abortAfter(timeout)
      ensure_active

      choose 'Absolute'
      fill_in '_.timeoutMinutes', :with => timeout
    end

    def abortWhenStuck
      ensure_active

      choose 'Likely stuck'
    end

    def useDescription
      ensure_active

      check '_.writingDescription'
    end

    private
    def ensure_active
      @job.ensure_config_page
      find(:path, "/hudson-plugins-build_timeout-BuildTimeoutWrapper").set true
    end
  end
end
