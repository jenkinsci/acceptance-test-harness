module Jenkins
  module PageArea
    include Capybara::DSL

    def initialize(pageobject, path_prefix)
      @pageobject = pageobject
      @path_prefix = path_prefix
    end

    def path(relative_path)
      return "#{@path_prefix}/#{relative_path}"
    end

    def self.included(receiver)
      receiver.extend Capybara::DSL
    end
  end
end
