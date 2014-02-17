require 'aspector'

# Monkey patching Capybara
module Capybara
  module Node
    class Element
      # Visually navigate to the element in order to interact with it.
      # Elements without path attribute are ignored
      def locate
        path = self[:path]

        if !path.nil?
          session.execute_script %{
              // Scroll to the element. It will appear at the top edge of the screen.
              element = document.evaluate(
                  "//*[@path='#{path}']", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null
              ).singleNodeValue.scrollIntoView();

              // Scroll a bit back so breadcrumbs are not hiding the element.
              window.scrollBy(0, -40);
          }
        end
        return self
      end

      def check
        ensure_checkable.set(true)
      end

      def uncheck
        ensure_checkable.set(false)
      end

      def plain
        native.text
      end

      def select(opition)
        self.find(:option, opition).select_option
      end

      private
      def ensure_checkable
        type = self[:type]
        elementDescription = !type ? tag_name : "#{tag_name} of type #{type}"

        if type != 'checkbox' && type != 'radio'
          raise "Element #{elementDescription} is not checkbox nor radio"
        end

        return self
      end
    end
  end
end

# Introduce ':path' selector to find an element with matching path attribute
Capybara.add_selector(:path) do
  xpath { |path| XPath.descendant[XPath.attr(:path) == path.to_s] }
end

# Automatically call .locate after finders returning exactly one result element
aspector(Capybara::Node::Finders) do
  logger.level = Aspector::Logging::WARN

  after :find, :first do |element, *args|
    return nil if element.nil?
    return element.locate
  end
end
