package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("hudson.model.FreeStyleProject")
public class FreeStyleJob extends Job {
    public FreeStyleJob(PageObject context, URL url, String name) {
        super(context, url, name);
    }
}
