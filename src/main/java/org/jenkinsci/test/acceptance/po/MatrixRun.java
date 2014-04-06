package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
public class MatrixRun extends Build {

    private final MatrixBuild build;

    public MatrixRun(MatrixConfiguration config, MatrixBuild build) {
        super(config, build.url("%s/", config.name));
        this.build = build;
    }

    public boolean exists() {
        return getJson().get("number").asInt() == build.getJson().get("number").asInt();
    }

    public MatrixConfiguration getConfiguration() {
        return (MatrixConfiguration) job;
    }

    public MatrixBuild getBuild() {
        return build;
    }
}
