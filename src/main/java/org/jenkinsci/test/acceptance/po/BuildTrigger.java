package org.jenkinsci.test.acceptance.po;

/**
 * Trigger other projects at the end of a build
 *
 * @author Kohsuke Kawaguchi
 */
@Describable("Build other projects")
public class BuildTrigger extends AbstractStep implements PostBuildStep {
    public final Control childProjects = control("childProjects");

    public final Control thresholdSuccess = control("threshold[SUCCESS]");
    public final Control thresholdFailure = control("threshold[FAILURE]");

    public BuildTrigger(Job parent, String path) {
        super(parent, path);
    }
}
