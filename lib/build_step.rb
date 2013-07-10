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
      return @@types[title][:label]
    end

    def self.type(title)
      return @@types[title][:type]
    end
  end
end
