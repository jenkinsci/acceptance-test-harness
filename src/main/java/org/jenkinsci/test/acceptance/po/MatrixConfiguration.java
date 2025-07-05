package org.jenkinsci.test.acceptance.po;

import java.net.URL;

/**
 * Specific combination of axis values.
 *
 * @author Kohsuke Kawaguchi
 */
public class MatrixConfiguration extends Job {
    public MatrixConfiguration(PageObject context, URL url, String name) {
        super(context, url, name);
    }
}
