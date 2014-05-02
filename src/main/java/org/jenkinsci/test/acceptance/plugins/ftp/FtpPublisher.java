package org.jenkinsci.test.acceptance.plugins.ftp;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Tobias Meyer
 */
@Describable("Send build artifacts over FTP")
public class FtpPublisher extends PostBuildStep {
    public FtpPublisher(Job parent, String path) {
        super(parent, path);
        String p = last(by.xpath(".//div[@name='publishers'][starts-with(@path,'%s/publishers')]", path)).getAttribute("path");
        defaultSite= new Site(page, p);
    }

    private Site defaultSite;
    public final Control add = control("repeatable-add");

    public Site getDefault() {
        return defaultSite;
    }

    public Site addServer() {
        add.click();
        String p = last(by.xpath(".//div[@name='publishers'][starts-with(@path,'%s/publishers')]", path)).getAttribute("path");
        return new Site(page, p);
    }

    public static class Site extends PageArea {
        public Site(PageObject parent, String path) {
            super(parent, path);
            String p = last(by.xpath(".//div[@name='transfers'][starts-with(@path,'%s/transfers')]", path)).getAttribute("path");
            defaultTransfer= new TransferArea(page, p);
        }

        public final Control add = control("repeatable-add");
        public final Control configName = control("configName");

        private TransferArea defaultTransfer;
        public TransferArea getDefaultTransfer() {
            return defaultTransfer;
        }

        public TransferArea addTransferSet()
        {
            add.click();
            String p = last(by.xpath(".//div[@name='transfers'][starts-with(@path,'%s/transfers')]", path)).getAttribute("path");
            return new TransferArea(page, p);
        }
    }

    public static class TransferArea extends PageArea {
        public TransferArea(PageObject parent, String path) {
            super(parent, path);
            Control advanced = control("advanced-button");
            advanced.click();
        }
        public final Control sourceFile = control("sourceFiles");
        public final Control removePrefix = control("removePrefix");
        public final Control remoteDirectory = control("remoteDirectory");
        public final Control excludes= control("excludes");
        public final Control patternSeparator = control("patternSeparator");
        public final Control noDefaultExcludes = control("noDefaultExcludes");
        public final Control makeEmptyDirs = control("makeEmptyDirs");
        public final Control flatten = control("flatten");
        public final Control remoteDirectorySDF = control("remoteDirectorySDF");
        public final Control cleanRemote = control("cleanRemote");
        public final Control asciiMode = control("asciiMode");
    }
}
