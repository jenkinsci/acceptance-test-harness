package org.jenkinsci.test.acceptance.plugins.nested_view;

import com.google.inject.Injector;
import java.net.URL;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.View;
import org.jenkinsci.test.acceptance.po.ViewsMixIn;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Nested View")
public class NestedView extends View {
    public final ViewsMixIn views = new ViewsMixIn(this);

    public NestedView(Injector injector, URL url) {
        super(injector, url);
    }

    public void setDefaultView(String name) {
        configure();
        // nested-view before 4ce2f3c (PR #82) used name="defaultView"; newer versions use databinding
        // (_.defaultViewName) and fill the options via AJAX, so wait for the option itself to appear.
        find(by.xpath(
                        "//select[@name='defaultView' or @name='_.defaultViewName']/option[normalize-space(.)='%1$s' or @value='%1$s']",
                        name))
                .click();
        save();
    }

    public void assertActiveView(String name) {
        find(by.xpath("//*[contains(@class, 'active') and (text()='%1$s' or a/text()='%1$s')]", name));
    }

    public void assertInactiveView(String name) {
        find(by.xpath("//*[contains(@class, 'inactive') or not(contains(@class, 'active'))]/a[text()='%s']", name));
    }

    public ViewsMixIn getViews() {
        return views;
    }
}
