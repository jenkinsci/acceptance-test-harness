package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class Job extends PageObject {
    public final String name;

    public Job(Injector injector, URL url, String name) {
        super(injector,url);
        this.name = name;
    }
}
