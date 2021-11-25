package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Archive the artifacts")
public class ArtifactArchiver extends AbstractStep implements PostBuildStep {
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
}
