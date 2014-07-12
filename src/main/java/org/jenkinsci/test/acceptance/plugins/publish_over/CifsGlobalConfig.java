package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
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
        add.click();
        String p = last(by.xpath(".//div[@name='instance'][starts-with(@path,'/jenkins-plugins-publish_over_cifs-CifsPublisherPlugin/')]")).getAttribute("path");
        return new CifSite(getPage(), p);
    }

    public static class CifSite extends GlobalSite {
        public CifSite(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control share = control("remoteRootDir");
    }
}