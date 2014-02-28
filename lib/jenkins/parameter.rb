require 'jenkins/pagearea'

module Jenkins
  class Parameter
    include Jenkins::PageArea

    attr_reader :name

    def self.add(job, title)
      find(:xpath, "//input[@name='parameterized']").check
      find(:xpath, "//button[text()='Add Parameter']").click
      find(:xpath, "//a[text()='#{title}']").click
      sleep 0.5
      prefix = all(:xpath, "//div[@name='parameter']").last[:path]

      return get(title).new(job, prefix)
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

    def name=(name)
      @name = name
      control('name').set(name)
    end

    def description=(description)
      control('description').set(value)
    end

    def fill_with(value)
      raise "To be implemented by a subclass"
    end

    #Override
    def path(relative_path)
      if page.current_url.ends_with? '/configure'
        # Path on /configure page
        super relative_path
      else
        # Path on /build page
        elem = find(:xpath, "//input[@name='name' and @value='#{name}']", visible: false)
        elem[:path].gsub(/\/name$/, '') + '/' + relative_path
      end
    end
  end

  class StringParameter < Parameter

    register 'String Parameter'

    def fill_with(value)
      control('value').set value
    end

    def default=(value)
      control('defaultValue').set(value)
    end
  end
end
