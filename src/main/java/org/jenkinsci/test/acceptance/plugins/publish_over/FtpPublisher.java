package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.po.*;

/**
 * Concrete Implementation for the FtpPublisher config page
 * @author Tobias Meyer
 */
@Describable("Send build artifacts over FTP")
public class FtpPublisher extends PublishGlobalPublisher {
    public FtpPublisher(Job parent, String path) {
        super(parent, path);
    }
    protected GlobalPublishSite CreatePublishSite(String p)
    {
        return new FTPPublishSite(getPage(), p);
    }
    public static class FTPPublishSite extends GlobalPublishSite {
        public FTPPublishSite(PageObject parent, String path) {
            super(parent, path);
        }
        protected FTPTransferArea CreateTransferArea(String p)
        {
            return new FTPTransferArea(getPage(), p);
        }

    }
    public static class FTPTransferArea extends GlobalTransferArea {
        public FTPTransferArea(PageObject parent, String path) {
            super(parent, path);
        }
        public final Control asciiMode = control("asciiMode");
    }
}
