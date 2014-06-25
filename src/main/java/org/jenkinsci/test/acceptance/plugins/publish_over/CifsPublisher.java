package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.po.*;

/**
 * Allows the  configuration for one job for the Publish Over Cifs plugin.
 * @author Tobias Meyer
 */
@Describable("Send build artifacts to a windows share")
public class CifsPublisher extends PublishGlobalPublisher implements PostBuildStep {
    public CifsPublisher(Job parent, String path) {
        super(parent, path);
    }

}
