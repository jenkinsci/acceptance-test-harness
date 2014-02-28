module Jenkins
  module Plugin
    module PrioritySorter
      class Global
        include PageArea

        def initialize(pageobject)
          super(pageobject, '/jenkins-advancedqueue-PrioritySorterConfiguration')
        end

        def strategy=(strategy)
          control('').select strategy
        end

        def priorities=(priorities)
          control('strategy/numberOfPriorities').set priorities
        end
      end

      class JobGroup
        include PageArea

        def initialize
          page = Page.new
          visit page.url
          click_button 'Add'

          path = all(:xpath, '//select[@name="view"]').last[:path][0..-6]
          super(page, path)
        end

        def priority=(priority)
          control('priority').select priority
        end

        def pattern=(pattern)
          control('useJobFilter').check
          control('useJobFilter/jobPattern').set pattern
        end

        def view=(view)
          control('view').set view
        end
      end

      private
      class Page < Jenkins::PageObject
        def initialize
          super($jenkins.base_url, 'Priority Sorter')
        end

        def url
          $jenkins.base_url + '/advanced-build-queue'
        end

        def add_group
          return JobGroup.new
        end
      end
    end
  end
end
