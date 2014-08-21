package org.jenkinsci.test.acceptance.update_center;

/**
 * @author Kohsuke Kawaguchi
 */
public interface UpdateCenterMetadataDecorator {
    void decorate(UpdateCenterMetadata ucm);
}
