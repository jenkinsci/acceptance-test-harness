module Jenkins
  class BuildStep
    include Capybara::DSL
    extend Capybara::DSL

    def self.add(job, title)

      click_button 'Add build step'
      click_link label(title)

      sleep 1
      prefix = all(:xpath, "//div[@name='builder']").last[:path]

      return type(title).new(job, prefix)
    end

    def initialize(job, path_prefix)
      @job = job
      @path_prefix = path_prefix
    end

    def path(relative_path)
      return "#{@path_prefix}/#{relative_path}"
    end

    @@types = Hash.new

    def self.register(title, label)
      @@types[title] = {type: self, label: label}
    end

    def self.label(title)
      return get(title)[:label]
    end

    def self.type(title)
      return get(title)[:type]
    end

    def self.get(title)
      return @@types[title] if @@types.has_key? title

      raise "Unknown build step type #{title}"
    end
  end

  class ShellBuildStep < BuildStep

    register 'Shell', 'Execute shell'

    def command(text)
      find(:path, path('command')).locate.set(text)
    end
  end
end
