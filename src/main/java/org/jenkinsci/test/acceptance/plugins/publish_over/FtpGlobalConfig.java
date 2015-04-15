package org.jenkinsci.test.acceptance.plugins.publish_over;

import javax.inject.Inject;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Tobias Meyer
 */
public class FtpGlobalConfig extends PublishGlobalConfig {
    @Inject
    public FtpGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/jenkins-plugins-publish_over_ftp-BapFtpPublisherPlugin");
    }

    @Override
    public FtpSite addSite() {
        add.click();
        String p = last(by.xpath(".//div[@name='instance'][starts-with(@path,'/jenkins-plugins-publish_over_ftp-BapFtpPublisherPlugin/')]")).getAttribute("path");
        return new FtpSite(getPage(), p);
    }

    public static class FtpSite extends GlobalSite {
        public FtpSite(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control password = control("encryptedPassword");
        public final Control remoteDir = control("remoteRootDir");
        public final Control useActiveData = control("useActiveData");
        public final Control disableMakeNestedDirs = control("disableMakeNestedDirs");
        public final Control controlEncoding = control("controlEncoding");
        public final Control disableRemoteVerification = control("disableRemoteVerification");
    }
}