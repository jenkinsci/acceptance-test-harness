require File.dirname(__FILE__) + "/pageobject.rb"

module Jenkins
  class View < PageObject
    attr_accessor :timeout

    def view_url
      @base_url + "/view/#{@name}"
    end

    def configure_url
      @base_url + "/view/#{@name}/configure"
    end

    def open
      visit(view_url)
    end

    def self.create_view(base_url, name, type)
      visit("#{@base_url}/newView")

      fill_in "name", :with => name
      find(:xpath, "//input[following-sibling::label[child::b[text()='#{type}']]]").set(true)
      click_button "OK"

      self.new(base_url, name)
   end
  end
end
