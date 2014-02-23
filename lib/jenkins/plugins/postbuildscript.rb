require 'jenkins/post_build_step'

module Plugins
  module PostBuildScript
    class Publisher < Jenkins::PostBuildStep

      register 'Post Build Script', 'Execute a set of scripts'

      def self.add(job)

        click_button 'Add post-build action'

        begin
          click_link 'Execute a set of scripts'
        rescue Capybara::ElementNotFound
          # The prefix was stripped in 0.12
          click_link '[PostBuildScript] - Execute a set of scripts'
        end

        sleep 2
        prefix = all(:xpath, "//div[@name='publisher']").last[:path]

        return self.new(job, prefix)
      end

      def add_step(title)
        Publisher.select_step(
            Jenkins::BuildStep.label(title),
            control('hetero-list-add[buildStep]')
        )

        prefix = all(:xpath, "//div[@name='buildStep']").last[:path]

        return Jenkins::BuildStep.type(title).new(self, prefix)
      end
    end
  end
end
