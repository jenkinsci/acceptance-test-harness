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

      def select_step(step, from)
        click_button from

        # With enough implementations registered the one we are looking for might
        # require scrolling in menu to become visible. This dirty hack stretch
        # yui menu so that all the items are visible.
        page.execute_script %{
            YAHOO.util.Dom.batch(
                document.querySelector(".yui-menu-body-scrolled"),
                function (el) {
                    el.style.height = "auto";
                    YAHOO.util.Dom.removeClass(el, "yui-menu-body-scrolled");
                }
            );
        }

        click_link step
      end
    end

    def self.included(receiver)
      receiver.extend Jenkins::PageArea
      receiver.extend Jenkins::Step::Static
    end
  end
end
