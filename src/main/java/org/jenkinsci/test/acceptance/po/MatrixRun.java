package org.jenkinsci.test.acceptance.po;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class MatrixRun extends Build {
    public MatrixRun(Job job, URL url) {
        super(job, url);
    }
}
