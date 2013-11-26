require 'jenkins/step'

module Jenkins
  class PostBuildStep
    include Jenkins::Step

    def self.add(job, title)

      select_step label(title), find(:path, '/hetero-list-add[publisher]')

      prefix = all(:xpath, "//div[@name='publisher']").last[:path]

      return type(title).new(job, prefix)
    end

    @@types = Hash.new

    def self.register(title, label)
      raise "#{title} already registered" if @@types.has_key? title

      @@types[title] = {type: self, label: label}
    end

    def self.get(title)
      return @@types[title] if @@types.has_key? title

      raise "Unknown #{self.name.split('::').last} type #{title}. #{@@types.keys}"
    end
  end

  class ArtifactArchiver < PostBuildStep

    register 'Artifact Archiver', 'Archive the artifacts'

    def self.add(job)

      click_button 'Add post-build action'

      title = 'Artifact Archiver'
      begin
        click_link label title
      rescue Capybara::ElementNotFound
        # When cloudbees-jsync-archiver installed (pending 5.0 and Jenkins 1.532+):
        click_link 'Archive artifacts (fast)'
      end

      sleep 1
      prefix = all(:xpath, "//div[@name='publisher']").last[:path]

      return type(title).new(job, prefix)
    end

    def includes(includes)
      control("artifacts").set includes
    end

    def excludes(excludes)
      control("advanced-button").click
      control("excludes").set excludes
    end

    def latest_only(latest)
      control("advanced-button").click
      control("latestOnly").set latest
    end
  end
end
