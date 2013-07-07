
require File.dirname(__FILE__) + "/pageobject.rb"
require File.dirname(__FILE__) + "/build.rb"

module Jenkins
  class MatrixConfiguration < PageObject
    attr_accessor :timeout, :matrix_job

    def initialize(base_url, name, matrix_job)
      @base_url = base_url
      @name = name
      @timeout = 60 # Default all builds for this job to a 60s timeout
      @matrix_job = matrix_job
    end

    def job_url
      @base_url + "/job/#{@matrix_job.name}/#{@name}"
    end
    
    def open
      visit(job_url)
    end

    def last_build
      return build("lastBuild") # Hacks!
    end

    def workspace
      Jenkins::Workspace.new(job_url)
    end

    def build(number)
      Jenkins::Build.new(@base_url, self, number)
    end

    def wait_for_build(number)
      build = self.build(number)
      start = Time.now
      while (build.in_progress? && ((Time.now - start) < @timeout))
        sleep 1
      end
    end
  end 
end
