package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.ExtensionPoint;

/**
 * A hook to tweak {@link UpdateCenterMetadata} to allow plugins and versions that are not in the update
 * center to be installed and tested.
 *
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public interface UpdateCenterMetadataDecorator {
    void decorate(UpdateCenterMetadata ucm);
}
