package org.jenkinsci.test.acceptance.plugins.ftp;

import javax.inject.Inject;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Tobias Meyer
 */
public class FtpGlobalConfig extends PageAreaImpl {
    @Inject
    JenkinsController controller;

    public final Control add = control("repeatable-add");

    @Inject
    public FtpGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/jenkins-plugins-publish_over_ftp-BapFtpPublisherPlugin");
    }

    public Site addSite() {
        add.click();
        String p = last(by.xpath(".//div[@name='instance'][starts-with(@path,'/jenkins-plugins-publish_over_ftp-BapFtpPublisherPlugin/')]")).getAttribute("path");
        return new Site(getPage(), p);
    }

    public static class Site extends PageAreaImpl {
        public Site(PageObject parent, String path) {
            super(parent, path);
            Control advanced = control("advanced-button");
            advanced.click();
        }

        public final Control name = control("name");
        public final Control hostname = control("hostname");
        public final Control username = control("username");
        public final Control password = control("encryptedPassword");
        public final Control remoteDir = control("remoteRootDir");
        public final Control port = control("port");
        public final Control timeout = control("timeout");
        public final Control useActiveData = control("useActiveData");
        public final Control disableMakeNestedDirs = control("disableMakeNestedDirs");
        public final Control controlEncoding = control("controlEncoding");
        public final Control disableRemoteVerification = control("disableRemoteVerification");
    }
}