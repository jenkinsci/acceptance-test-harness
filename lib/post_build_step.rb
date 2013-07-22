require File.dirname(__FILE__) + "/step.rb"

module Jenkins
  class PostBuildStep
    include Jenkins::Step

    def self.add(job, title)

      click_button 'Add post-build action'
      click_link label(title)

      sleep 1
      prefix = all(:xpath, "//div[@name='publisher']").last[:path]

      return type(title).new(job, prefix)
    end
  end
end
