# Monkey patching Capybara
module Capybara
  module Node
    class Element
      # Visually navigate to the element in order to interact with it.
      def locate
        path = self[:path]
        session.execute_script %{
            // Scroll to the element. It will appear at the top edge of the screen.
            element = document.evaluate(
                "//*[@path='#{path}']", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null
            ).singleNodeValue.scrollIntoView();

            // Scroll a bit back so breadcrumbs are not hiding the element.
            window.scrollBy(0, -40);
        }
        return self
      end
    end
  end
end
