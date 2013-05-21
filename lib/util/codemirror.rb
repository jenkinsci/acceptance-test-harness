module Jenkins
  module Util
    # Interact with code mirror instance.
    #
    # This implementation creates new instance as it can not reach the original
    # one. Ugly but working.
    class CodeMirror

      def initialize(page, textarea)
        @page = page
        @textarea = textarea.locate
      end


      def set_content(content)
        @page.execute_script %{
          #{code_mirror_instance}.setValue("#{content}")
        }
      end

      private
      def code_mirror_instance
        return %{
            CodeMirror.fromTextArea(#{get_element(@textarea[:path])})
        }
      end

      def get_element(path)

        return %{document.evaluate(
            "//*[@path='#{path}']", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null
        ).singleNodeValue}
      end
    end
  end
end
