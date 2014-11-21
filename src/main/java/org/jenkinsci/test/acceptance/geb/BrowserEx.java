package org.jenkinsci.test.acceptance.geb;

import com.google.inject.Injector;
import geb.Browser;
import geb.Configuration;
import geb.Page;

/**
 * {@link Browser} subtype that creates pages objects via Guice so that POs can inject other parameters.
 *
 * <p>
 * This is useful to call into Java page objects that implement various logics.
 *
 * @author Kohsuke Kawaguchi
 */
public class BrowserEx extends Browser {
    public final Injector injector;

    BrowserEx(Injector injector, Configuration conf) {
        super(conf);
        this.injector = injector;
    }

    @Override
    public <T extends Page> T createPage(Class<T> pageType) {
        return pageType.cast(injector.getInstance(pageType).init(this));
    }
}
