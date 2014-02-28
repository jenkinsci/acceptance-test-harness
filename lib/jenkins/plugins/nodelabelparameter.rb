module Jenkins
  module Plugin
    module NodeLabel

      class NodeParameter < Jenkins::Parameter
        register 'Node'

        def fill_with(value)
          select = control('labels')
          for node in value.split /,[ ]?/ do
            select.select(node)
          end
        end

        def allow_multiple
          control('triggerIfResult[allowMultiSelectionForConcurrentBuilds]').set true
        end
      end

      class LabelParameter < Jenkins::Parameter
        register 'Label'

        def fill_with(value)
          control('label').set value
        end
      end
    end
  end
end
