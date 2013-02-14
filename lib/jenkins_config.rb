#!/usr/bin/env ruby

require File.dirname(__FILE__) + "/pageobject.rb"

module Jenkins
  class JenkinsConfig < PageObject
    
    def initialize(*args)
      super(*args)
    end
    
    def configure_url
      @base_url + "/configure"
    end

    def open
      visit(configure_url)
    end
    
    def add_tool(name)
      click_button(name)
    end

    def self.get(base_url, name)
      self.new(base_url, name)
    end

  end
end
