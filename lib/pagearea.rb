module Jenkins
  module PageArea
    include Capybara::DSL

    def initialize(parent, path_prefix)
      @parent = parent
      @path_prefix = path_prefix
    end

    def path(relative_path)
      return "#{@path_prefix}/#{relative_path}"
    end

    def control(relative_path)
      return find(:path, path(relative_path))
    end

    def self.included(receiver)
      receiver.extend Capybara::DSL
    end
  end
end
