package org.jenkinsci.test.acceptance.po;

@Describable("Record fingerprints of files to track usage")
public class Fingerprint extends AbstractStep implements PostBuildStep {
    public final Control targets = control("targets");

    public Fingerprint(Job parent, String path) {
        super(parent, path);
    }
}
