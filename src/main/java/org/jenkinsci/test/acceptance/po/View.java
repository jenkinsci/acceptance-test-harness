package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class View extends PageObject {
    public View(Injector injector, URL url) {
        super(injector, url);
    }
}
