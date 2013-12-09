require 'jenkins/pageobject'

module Jenkins
  class Workspace < PageObject
    attr_accessor :url

    def initialize(base_url)
      @url = base_url + "/ws"
      super(url, "#{url}: Workspace")
    end

    def open
      visit(url)
    end

    def wipe_out!
      open
      begin
        click_link 'Wipe Out Current Workspace'
        page.driver.browser.switch_to.alert.accept
      rescue Capybara::ElementNotFound
        click_link 'Wipe Out Workspace'
        click_button 'Yes'
      end

    end

    def contains(filename)
      open
      page.has_xpath? "//table[@class='fileList']//a[text()='#{filename}']"
    end
  end
end
