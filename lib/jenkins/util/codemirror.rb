module Jenkins
  module Util
    class CodeMirror
      include Capybara::DSL

      def initialize(page, textarea)
        @page = page
        @textarea = find(:path, textarea, :visible => false)
      end

      def set_content(content)
        @page.execute_script script content
      end

      private
      # This implementation relies on 'codemirrorObject' attached to the textarea.
      # In case it does not exist (plugin creates codemirror enhanced textarea
      # in a nonstandard way) it creates new instance as it can not reach
      # the original one. This used to work for scriptler.
      def script(content)
        content = content.gsub "\n", '\n'
        xpath = "//*[@path='#{@textarea[:path]}']"
        return %{
            textarea = document.evaluate(
                "#{xpath}", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null
            ).singleNodeValue;

            codemirror = textarea.codemirrorObject;
            if (codemirror == null) {
              codemirror = CodeMirror.fromTextArea(textarea)
            }
            codemirror.setValue("#{content}");
            codemirror.focus();
          }
      end
    end
  end
end
