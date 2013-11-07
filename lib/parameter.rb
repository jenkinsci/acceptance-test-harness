require File.dirname(__FILE__) + "/pagearea.rb"

module Jenkins
  class Parameter
    include Jenkins::PageArea

    def self.add(job, title)
      find(:xpath, "//input[@name='parameterized']").check
      find(:xpath, "//button[text()='Add Parameter']").click
      find(:xpath, "//a[text()='#{title}']").click
      sleep 0.1
      prefix = all(:xpath, "//div[@name='parameter']").last[:path]

      return get(title).new(job, prefix)
    end

    def description(description)
      control('description').set(value)
    end

    @@types = Hash.new

    # Register Parameter type
    def self.register(title)
      @@types[title] = self
    end

    # Get type by title
    def self.get(title)
      return @@types[title]
    end
  end

  class StringParameter < Parameter

    register 'String Parameter'

    def name(name)
      control('name').set(name)
    end

    def default(value)
      control('defaultValue').set(value)
    end
  end
end
