package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;

import javax.inject.Inject;

/**
 * Allows the global configuration for the Publish Over Cifs plugin.
 * @author Tobias Meyer
 */
public class CifsGlobalConfig extends PublishGlobalConfig {
    @Inject
    public CifsGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/jenkins-plugins-publish_over_cifs-CifsPublisherPlugin");
    }

    public CifSite addSite() {
        String path = createPageArea("instance", new Runnable() {
            @Override public void run() {
                add.click();
            }
        });
        return new CifSite(getPage(), path);
    }

    public static class CifSite extends GlobalSite {
        public CifSite(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control share = control("remoteRootDir");
    }
}
