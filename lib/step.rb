require File.dirname(__FILE__) + "/pagearea.rb"

module Jenkins
  module Step
    include Jenkins::PageArea

    module Static
      def label(title)
        return get(title)[:label]
      end

      def type(title)
        return get(title)[:type]
      end
    end

    def self.included(receiver)
      receiver.extend Jenkins::PageArea
      receiver.extend Jenkins::Step::Static
    end
  end
end
