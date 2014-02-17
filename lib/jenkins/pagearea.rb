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

    # Find control on given path relative to the pagearea
    #
    # Several paths can be provided to find the first matching element. Useful
    # when element path changed between versions.
    def control(*relative_paths)
      exception = nil
      for p in relative_paths do
        begin
          return find :path, path(p)
        rescue Capybara::ElementNotFound => e
          exception = e
        end
      end

      raise e
    end

    def self.included(receiver)
      receiver.extend Capybara::DSL
    end
  end
end
