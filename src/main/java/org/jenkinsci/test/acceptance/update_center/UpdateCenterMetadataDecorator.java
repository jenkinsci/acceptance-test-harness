package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.ExtensionPoint;

/**
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public interface UpdateCenterMetadataDecorator {
    void decorate(UpdateCenterMetadata ucm);
}
