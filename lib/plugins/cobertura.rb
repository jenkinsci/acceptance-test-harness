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
      @job.url + "/cobertura"
    end

    def packages_coverage
      visit url
      find(:xpath, '//th[text()="Packages"]/../td').text.to_i
    end

    def files_coverage
      visit url
      find(:xpath, '//th[text()="Files"]/../td').text.to_i
    end

    def classes_coverage
      visit url
      find(:xpath, '//th[text()="Classes"]/../td').text.to_i
    end

    def methods_coverage
      visit url
      find(:xpath, '//th[text()="Methods"]/../td').text.to_i
    end

    def lines_coverage
      visit url
      find(:xpath, '//th[text()="Lines"]/../td').text.to_i
    end

    def conditionals_coverage
      visit url
      find(:xpath, '//th[text()="Conditionals"]/../td').text.to_i
    end
  end
end
