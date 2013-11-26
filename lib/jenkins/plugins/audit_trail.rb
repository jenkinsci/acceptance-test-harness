require 'jenkins/pageobject'

module Plugins
  class AuditTrail < Jenkins::PageObject

    def initialize(base_url)
      super(base_url, 'Audit Trail Log')

      begin
        sleep 1
        open
      end until page.has_xpath? "//h1[text()='Audit Trail']"
    end

    def url
      base_url + '/log/Audit Trail/'
    end

    def open
      visit url
    end

    def events
      open
      events = []
      all(:css, '#main-panel pre').each do |row|
        matcher = /((?:\/\w+)+.*?) by /.match(row.text)
        next if matcher.nil? # Earlier versions used one element per log entry newer use two
        events << matcher[1]
      end

      return events
    end

    def empty?
      open
      first(:css, '#main-panel pre').nil?
    end
  end
end
