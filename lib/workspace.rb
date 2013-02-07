require File.dirname(__FILE__) + "/pageobject.rb"

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

    def contains(filename)
      open
      page.has_xpath? "//table[@class='fileList']//a[text()='#{filename}']"
    end
  end
end
