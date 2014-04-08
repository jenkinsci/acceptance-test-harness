package org.jenkinsci.test.acceptance.plugins.nested_view;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.View;
import org.jenkinsci.test.acceptance.po.ViewsMixIn;

import java.net.URL;

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
        find(by.xpath("//table[@id='viewList']//td[@class='active' and text()='%s']",name));
    }

    public void assertInactiveView(String name) {
        find(by.xpath("//table[@id='viewList']//td[@class='inactive' and text()='%s']",name));
    }
}
