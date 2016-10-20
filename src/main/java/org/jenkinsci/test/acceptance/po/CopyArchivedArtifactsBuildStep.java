package org.jenkinsci.test.acceptance.po;

@Describable("Copy archived artifacts from remote/local job")
public class CopyArchivedArtifactsBuildStep extends AbstractStep implements BuildStep {

    public final Control sourceJob = control("");
    public final Control timeout = control("timeout");
    public final Control includes = control("includes");

    public CopyArchivedArtifactsBuildStep(Job parent, String path) {
        super(parent, path);
    }
}
