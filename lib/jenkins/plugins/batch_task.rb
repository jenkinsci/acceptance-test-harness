require 'jenkins/pagearea'

module Plugin
  module BatchTask
    class Declaration
      include Jenkins::PageArea

      PREFIX = '/properties/hudson-plugins-batch_task-BatchTaskProperty/on'

      def self.add(job)
        checkbox = find(:path, PREFIX)
        if !checkbox.checked?
          checkbox.check
        else
          click_button 'Add another task...'
          sleep 1
        end

        path_prefix = all(:xpath, "//input[@name='batch-task.name']")
            .last[:path][0..-6]

        return Declaration.new(job, path_prefix)
      end

      def name=(name)
        control('name').set name
      end

      def script=(script)
        control('script').set script
      end
    end

    class Trigger < Jenkins::PostBuildStep
      register 'Invoke batch tasks', 'Invoke batch tasks'

      def task(job,task)
        control('configs/project').set job
        control('configs/task').click #options are not evailable while project has focus
        control('configs/task').select task
      end

      def allow_unstable
        control('evenIfUnstable').check
      end
    end

    class Task < Jenkins::PageObject

      def initialize(parent, name)
        super(parent.base_url, name)
        @parent = parent
      end

      def url
        @parent.url + '/batchTasks/task/' + name
      end

      def build!
        visit url
        click_link 'Build Now'
      end

      def exists?
        visit url
        page.has_content? '#1-1'
      end
    end
  end
end
