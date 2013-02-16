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

      with_hidden_stickers do
        chk = find(:xpath, "//input[starts-with(@name,'plugin.#{name}.')]")
        begin
          chk.click()
        rescue => e
        end
        unless chk.selected?
          # not really sure why this is needed, by on Sauce OnDemand Windows 2003 + Firefox 16 (at least),
          # the above click() command still doesn't click it. Maybe it still thinks the checkbox
          # is under the breadcrumb, even though we hide it
          page.execute_script "window.scrollBy(0,-50)"
          chk.click()
        end
      end

      click_button 'Install'
    end

    def installed?(name)
      visit "#{url}/installed"
      page.has_xpath?("//input[@url='plugin/#{name}']")
    end
  end
end
