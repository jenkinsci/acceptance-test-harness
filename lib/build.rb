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

    def open
      visit(build_url)
    end

    def json_api_url
      "#{build_url}/api/json"
    end

    def console
      @console ||= begin
        visit("#{build_url}/console")
        find(:xpath, "//pre").text
      end
    end

    def result?
      @result ||= self.json['result']
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
      data = self.json
      return data['building'] || result?.nil?
    end
  end
end
