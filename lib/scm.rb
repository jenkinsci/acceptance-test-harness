module Jenkins
  class Scm
    include Capybara::DSL
    extend Capybara::DSL

    def initialize(job, path_prefix)
      @job = job
      @path_prefix = path_prefix
    end

    def path(relative_path)
      return "#{@path_prefix}/#{relative_path}"
    end

    @@types = Hash.new

    # Register SCM type
    def self.register(title)
      @@types[title] = self
    end

    # Get type by title
    def self.get(title)
      return @@types[title]
    end
  end
end
