package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.po.*;

/**
 * Common class for Publisher config pages
 * @author Tobias Meyer
 */
public abstract class PublishGlobalPublisher extends AbstractStep  implements PostBuildStep {
    public PublishGlobalPublisher(Job parent, String path) {
        super(parent, path);
        String p = last(by.xpath(".//div[@name='publishers'][starts-with(@path,'%s/publishers')]", path)).getAttribute("path");
        defaultSite = CreatePublishSite(p);
    }


    private GlobalPublishSite defaultSite;
    public final Control add = control("repeatable-add");

    public GlobalPublishSite getDefault() {
        return defaultSite;
    }

    public GlobalPublishSite addServer() {
        add.click();
        String p = last(by.xpath(".//div[@name='publishers'][starts-with(@path,'%s/publishers')]", getPath())).getAttribute("path");
        return CreatePublishSite(p);
    }
    protected GlobalPublishSite CreatePublishSite(String p)
    {
        return new GlobalPublishSite(getPage(), p);
    }
    public static class GlobalPublishSite extends PageAreaImpl {
        public GlobalPublishSite(PageObject parent, String path) {
            super(parent, path);
            String p = last(by.xpath(".//div[@name='transfers'][starts-with(@path,'%s/transfers')]", path)).getAttribute("path");
            defaultTransfer = CreateTransferArea(p);
        }

        protected GlobalTransferArea CreateTransferArea(String p)
        {
            return new GlobalTransferArea(getPage(), p);
        }
        public final Control add = control("repeatable-add");
        public final Control configName = control("configName");

        private GlobalTransferArea defaultTransfer;

        public GlobalTransferArea getDefaultTransfer() {
            return defaultTransfer;
        }

        public GlobalTransferArea addTransferSet() {
            add.click();
            String p = last(by.xpath(".//div[@name='transfers'][starts-with(@path,'%s/transfers')]", getPath())).getAttribute("path");
            return CreateTransferArea(p);
        }
    }

    public static class GlobalTransferArea extends PageAreaImpl {
        public GlobalTransferArea(PageObject parent, String path) {
            super(parent, path);
            Control advanced = control("advanced-button");
            advanced.click();
        }

        public final Control sourceFile = control("sourceFiles");
        public final Control removePrefix = control("removePrefix");
        public final Control remoteDirectory = control("remoteDirectory");
        public final Control excludes = control("excludes");
        public final Control patternSeparator = control("patternSeparator");
        public final Control noDefaultExcludes = control("noDefaultExcludes");
        public final Control makeEmptyDirs = control("makeEmptyDirs");
        public final Control flatten = control("flatten");
        public final Control remoteDirectorySDF = control("remoteDirectorySDF");
        public final Control cleanRemote = control("cleanRemote");
    }
}
