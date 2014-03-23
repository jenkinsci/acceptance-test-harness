package org.jenkinsci.test.acceptance.po;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class MatrixBuild extends Build {
    public MatrixBuild(Job job, URL url) {
        super(job, url);
    }
}
