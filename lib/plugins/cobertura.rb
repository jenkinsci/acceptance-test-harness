#!/usr/bin/env ruby

require 'capybara'
require 'capybara/dsl'

module Plugins
  class Cobertura
    include Capybara::DSL
    extend Capybara::DSL

    def initialize(job)
      @job = job
    end

    def url
      @job.job_url + "/cobertura"
    end


    def packages_coverage
      visit url
      find(:xpath, '//td[text()="Cobertura Coverage Report"]/following-sibling::td[1]').text.to_i
    end

    def files_coverage
      visit url
      find(:xpath, '//td[text()="Cobertura Coverage Report"]/following-sibling::td[2]').text.to_i
    end

    def classes_coverage
      visit url
      find(:xpath, '//td[text()="Cobertura Coverage Report"]/following-sibling::td[3]').text.to_i
    end

    def methods_coverage
      visit url
      find(:xpath, '//td[text()="Cobertura Coverage Report"]/following-sibling::td[4]').text.to_i
    end

    def lines_coverage
      visit url
      find(:xpath, '//td[text()="Cobertura Coverage Report"]/following-sibling::td[5]').text.to_i
    end

    def conditionals_coverage
      visit url
      find(:xpath, '//td[text()="Cobertura Coverage Report"]/following-sibling::td[6]').text.to_i
    end


  end
end
