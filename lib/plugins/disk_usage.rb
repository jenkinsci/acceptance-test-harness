require File.dirname(__FILE__) + "/../pageobject.rb"

module Plugins
  class DiskUsage < Jenkins::PageObject

    def update
      visit "#{@baseurl}/plugin/disk-usage"
      find(:xpath, "//button[text()='Record Disk Usage']").click
    end

    def wait_for_update(log_watcher)
      log_watcher.wait_until_logged(/Finished Project disk usage. \d+ ms/, 5)
    end

  end
end
