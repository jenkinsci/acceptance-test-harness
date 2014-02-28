#!/usr/bin/env ruby

require 'capybara'
require 'capybara/dsl'

module Plugins
  class Checkstyle
    include Capybara::DSL
    extend Capybara::DSL

    def initialize(job)
      @job = job
    end

    def url
      @job.url + "/checkstyle"
    end

    def high_prio_url
      @job.last_build.url + "checkstyleResult/HIGH"
    end

    def warnings_number
      visit url
      find(:xpath, '//table[@id="summary"]/tbody/tr/td[@class="pane"][1]').text.to_i
    end

    def new_warnings_number
      visit url
      find(:xpath, '//table[@id="summary"]/tbody/tr/td[@class="pane"][2]/a').text.to_i
    end

    def fixed_warnings_number
      visit url
      find(:xpath, '//table[@id="summary"]/tbody/tr/td[@class="pane"][3]').text.to_i
    end

    def high_warnings_number
      visit url
      find(:xpath, '//table[@id="analysis.summary"]/tbody/tr/td[@class="pane"][2]/a').text.to_i
    end

    def normal_warnings_number
      visit url
      find(:xpath, '//table[@id="analysis.summary"]/tbody/tr/td[@class="pane"][3]').text.to_i
    end

    def low_warnings_number
      visit url
      find(:xpath, '//table[@id="analysis.summary"]/tbody/tr/td[@class="pane"][4]').text.to_i
    end

  end
end
