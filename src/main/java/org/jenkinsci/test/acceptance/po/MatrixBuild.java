package org.jenkinsci.test.acceptance.po;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class MatrixBuild extends Build {
    public MatrixBuild(Job job, URL url) {
        super(job.as(MatrixProject.class), url);
    }

    public MatrixProject getJob() {
        return (MatrixProject) job;
    }

    public List<MatrixRun> getConfigurations() {
        List<MatrixRun> builds = new ArrayList<>();
        for (MatrixConfiguration c : getJob().getConfigurations()) {
            builds.add(new MatrixRun(c, this));
        }
        return builds;
    }

    public MatrixRun getConfiguration(String name) {
        return new MatrixRun(getJob().getConfiguration(name), this);
    }
}
