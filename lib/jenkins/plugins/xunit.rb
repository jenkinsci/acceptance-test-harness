module Plugins
  module XUnit
    class Publisher < Jenkins::PostBuildStep
      register 'xUnit', 'Publish xUnit test result report'

      def add_tool(kind)
        control('hetero-list-add[tools]').click
        click_link kind
        xpath = "//div[starts-with(@path, '#{path("tools")}')]"
        sleep 0.1
        path = all(:xpath, xpath).last[:path]
        return Tool.new self, path
      end
    end

    class Tool
      include Jenkins::PageArea

      def pattern=(pattern)
        control('pattern').set(pattern)
      end
    end
  end
end
