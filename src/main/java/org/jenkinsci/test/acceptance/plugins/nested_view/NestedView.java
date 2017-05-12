package org.jenkinsci.test.acceptance.plugins.nested_view;

import java.net.URL;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.View;
import org.jenkinsci.test.acceptance.po.ViewsMixIn;

import com.google.inject.Injector;

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
        find(by.input("defaultView")).findElement(by.option(name)).click();
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
