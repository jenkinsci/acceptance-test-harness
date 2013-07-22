require File.dirname(__FILE__) + "/step.rb"

module Jenkins
  class BuildStep
    include Jenkins::Step

    def self.add(job, title)

      click_button 'Add build step'
      click_link label(title)

      sleep 1
      prefix = all(:xpath, "//div[@name='builder']").last[:path]

      return type(title).new(job, prefix)
    end
  end

  class ShellBuildStep < BuildStep

    register 'Shell', 'Execute shell'

    def command(text)
      find(:path, path('command')).locate.set(text)
    end
  end
end
