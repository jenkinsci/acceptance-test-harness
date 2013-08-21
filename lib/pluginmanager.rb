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

      wait_for("//span[@id='completionMarker' and text()='Done']")

      @updated = true
      # This is totally arbitrary, it seems that the Available page doesn't
      # update properly if you don't sleep a bit
      sleep 5
    end

    def installed?(name)
      visit "#{url}/installed"
      page.has_xpath?("//input[@url='plugin/#{name}']")
    end

    def install_plugin!(name)

      return if installed?(name)

      unless @updated
        check_for_updates
      end

      visit "#{url}/available"
      find(:xpath, "//input[starts-with(@name,'plugin.#{name}.')]").locate.set(true)
      find_button('Install').click

      start = Time.now.to_i
      # wait for plugin to appear in the interface
      until installed?(name) do
        if Time.now.to_i - start > 180
          throw "Plugin installation took too long"
        end
        sleep 1
      end

      installation_time = Time.now.to_i - start
      if installation_time > 30
        puts "Plugin installation took #{installation_time} seconds"
      end
    end
  end
end
