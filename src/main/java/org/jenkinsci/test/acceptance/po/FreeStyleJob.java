package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("hudson.model.FreeStyleProject")
public class FreeStyleJob extends Job {
    public FreeStyleJob(Injector injector, URL url, String name) {
        super(injector, url, name);
    }
}
