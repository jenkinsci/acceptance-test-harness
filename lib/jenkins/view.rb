require "jenkins/pageobject"

module Jenkins
  class View < PageObject
    attr_accessor :timeout

    def url
      @base_url + "/view/#{@name}"
    end

    def configure_url
      @base_url + "/view/#{@name}/configure"
    end

    def open
      visit(url)
    end

    def save
      click_button "OK"
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
