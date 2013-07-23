module Jenkins
  class Parameter
    include Jenkins::PageArea

    def self.add(job, title)
      find(:xpath, "//input[@name='parameterized']").check
      find(:xpath, "//button[text()='Add Parameter']").locate.click
      find(:xpath, "//a[text()='#{title}']").click
      sleep 0.1
      prefix = all(:xpath, "//div[@name='parameter']").last[:path]

      return get(title).new(job, prefix)
    end

    def description(description)
      find(:path, path('description')).set(value)
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
      find(:path, path('name')).set(name)
    end

    def default(value)
      find(:path, path('defaultValue')).set(value)
    end
  end
end
