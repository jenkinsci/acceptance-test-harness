#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require File.dirname(__FILE__) + "/pageobject.rb"

module Jenkins
  class Build < PageObject
    attr_accessor :job, :number

    def initialize(base_url, job, number)
      @base_url = base_url
      @job = job
      @number = number
      super(base_url, "#{job}/#{number}")
    end

    def build_url
      @job.job_url + "/#{@number}"
    end

    def artifact_url(artifact)
      build_url + "/artifact/#{artifact}"
    end

    def console_url()
      "#{build_url}/console"
    end

    def open
      visit(build_url)
    end

    def json_api_url
      "#{build_url}/api/json"
    end

    def console
      @console ||= begin
        visit(console_url)
        find(:xpath, "//pre").text
      end
    end

    def configuration(name)
      return Jenkins::BuiltConfiguration.new(
          self, Jenkins::Configuration.new(@job, name)
      )
    end

    def result?
      wait_until_finished

      @result ||= self.json['result']
    end

    def wait_until_finished
      wait_until_started

      while in_progress?
        sleep 1
      end

      return self
    end

    def wait_until_started
      loop do
        begin
          self.json
          return self# We have json. Build has started
        rescue Exception
          sleep 1
          next # retry
        end
      end

      return self
    end

    def succeeded?
      result? == 'SUCCESS'
    end

    def unstable?
      result? == 'UNSTABLE'
    end

    def failed?
      result? == 'FAILED'
    end

    def in_progress?

      return false if !@result.nil?

      data = self.json
      return data['building'] || data['result'].nil?
    end
  end
end
