package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import java.net.URL;

/**
 * Specific combination of axis values.
 *
 * @author Kohsuke Kawaguchi
 */
public class MatrixConfiguration extends Job {
    public MatrixConfiguration(Injector injector, URL url, String name) {
        super(injector, url, name);
    }
}
