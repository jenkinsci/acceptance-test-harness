package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("List View")
public class ListView extends View {
    public ListView(Injector injector, URL url) {
        super(injector, url);
    }
}
