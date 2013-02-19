#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

require File.dirname(__FILE__) + "/pageobject.rb"

module Jenkins
  class PluginManager < PageObject
    def initialize(*args)
      super(*args)
      @updated = false
    end

    def url
      @base_url + "/pluginManager"
    end

    def open
      visit url
    end

    def check_for_updates
      visit "#{url}/checkUpdates"

      wait_for("//span[@id='completionMarker' and text()='Done']", :with => :xpath)

      @updated = true
      # This is totally arbitrary, it seems that the Available page doesn't
      # update properly if you don't sleep a bit
      sleep 5
    end

    def install_plugin(name)
      unless @updated
        check_for_updates
      end

      visit "#{url}/available"

      find(:xpath, "//input[starts-with(@name,'plugin.#{name}.')]").locate.set(true)

      find_button('Install').click
    end

    def installed?(name)
      visit "#{url}/installed"
      page.has_xpath?("//input[@url='plugin/#{name}']")
    end
  end
end
