module Jenkins
  module Step
    include Capybara::DSL

    def initialize(job, path_prefix)
      @job = job
      @path_prefix = path_prefix
    end

    def path(relative_path)
      return "#{@path_prefix}/#{relative_path}"
    end

    module ClassMethods
      include Capybara::DSL

      def register(title, label)
        @@types[title] = {type: self, label: label}
      end

      @@types = Hash.new

      def label(title)
        return get(title)[:label]
      end

      def type(title)
        return get(title)[:type]
      end

      def get(title)
        return @@types[title] if @@types.has_key? title

        raise "Unknown #{self.name.split('::').last} type #{title}"
      end
    end

    def self.included(base)
      base.extend(ClassMethods)
    end
  end
end
