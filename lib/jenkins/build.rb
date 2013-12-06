#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require 'jenkins/pageobject'

module Jenkins
  class Build < PageObject
    attr_accessor :job, :number

    def initialize(base_url, job, number)
      @base_url = base_url
      @job = job
      @number = number
      super(base_url, "#{job}/#{number}")
    end

    def url
      @job.url + "/#{@number}"
    end

    def artifact_url(artifact)
      url + "/artifact/#{artifact}"
    end

    def console_url()
      "#{url}/console"
    end

    def open
      visit(url)
    end

    def json_api_url
      "#{url}/api/json"
    end

    def console
      @console ||= begin
        visit(console_url)
        find(:xpath, "//pre").plain
      rescue Capybara::Ambiguous
        find(:xpath, "//pre[@id='out']").plain
      end
    end

    def node
      slave = json['builtOn']
      return 'master' if slave == ''
      return slave
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

    # @return [Jenkins::Build]    this object itself
    def wait_until_started
      wait_for_cond do
        has_started?
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

    def has_started?

      return true if !@result.nil?

      begin
        self.json
        return true # We have json. Build has started
      rescue Exception
        return false
      end
    end

    def in_progress?

      return false if !@result.nil?

      return false if !has_started?

      data = self.json
      return data['building'] || data['result'].nil?
    end

    # assert that the build should have succeeded
    def should_succeed
      succeeded?.should eql(true), "\nConsole output:\n#{console}\n\n"
    end
  end
end
