package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Archive the artifacts")
public class ArtifactArchiver extends PostBuildStep {
    public ArtifactArchiver(Job parent, String path) {
        super(parent, path);
    }

    public ArtifactArchiver includes(String value) {
        control("artifacts").set(value);
        return this;
    }

    public ArtifactArchiver excludes(String value) {
        control("advanced-button").click();
        control("excludes").set(value);
        return this;
    }

    public ArtifactArchiver latestOnly(boolean check) {
        control("advanced-button").click();
        control("latestOnly").check(check);
        return this;
    }
}
