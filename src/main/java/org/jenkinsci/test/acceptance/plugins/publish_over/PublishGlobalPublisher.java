package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.po.*;

/**
 * Common class for Publisher config pages
 * @author Tobias Meyer
 */
public abstract class PublishGlobalPublisher extends AbstractStep  implements PostBuildStep {
    /**
     * Constructor for the PublishGlobalPublisher
     * @param parent Job to configure
     * @param path path to the Publisher
     */
    public PublishGlobalPublisher(Job parent, String path) {
        super(parent, path);
        String p = last(by.xpath(".//div[@name='publishers'][starts-with(@path,'%s/publishers')]", path)).getAttribute("path");
        defaultSite = createPublishSite(p);
    }

    /**
     * Every publisher creates a defaultConfigSite with a default Server.
     * In must publishers this default Site is empty.
     */
    private final GlobalPublishSite defaultSite;
    /**
     * Add Button for Server
     */
    public final Control add = control("repeatable-add");

    /**
     * Get the default Server(site)
     * @return default Server(site)
     */
    public GlobalPublishSite getDefault() {
        return defaultSite;
    }

    /**
     * Add one Server
     * @return GlobalPublishSite of the added Server
     */
    public GlobalPublishSite addServer() {
        String path = createPageArea("publishers", new Runnable() {
            @Override public void run() {
                add.click();
            }
        });
        return createPublishSite(path);
    }

    /**
     * Abstraction to create a GlobalPublishSite
     * @param p XML Path of the Element as String
     * @return GlobalPublishSite
     */
    protected GlobalPublishSite createPublishSite(String p) {
        return new GlobalPublishSite(getPage(), p);
    }

    /**
     * The GlobalPublishSite is the configuration for one server of the publish over xxx plugin.
     */
    public static class GlobalPublishSite extends PageAreaImpl {
        public GlobalPublishSite(PageObject parent, String path) {
            super(parent, path);
            String p = last(by.xpath(".//div[@name='transfers'][starts-with(@path,'%s/transfers')]", path)).getAttribute("path");
            defaultTransfer = createTransferArea(p);
        }

        /**
         * Creates a GlobalTransferArea for a publish over xxx plugin.
         * Can be overwritten for concrete implementation
         * @param p XML Path of the Element as String
         * @return GlobalTransferArea
         */
        protected GlobalTransferArea createTransferArea(String p)
        {
            return new GlobalTransferArea(getPage(), p);
        }

        /**
         * Add Button for the transfers
         */
        public final Control add = control("repeatable-add");
        /**
         * Name for this configuration.
         */
        public final Control configName = control("configName");

        /**
         * Default transfer Area
         */
        private final GlobalTransferArea defaultTransfer;

        /**
         * Gets the default transfer Area
         * @return GlobalTransferArea
         */
        public GlobalTransferArea getDefaultTransfer() {
            return defaultTransfer;
        }

        /**
         * Adds a Transfer Area
         * @return GlobalTransferArea describing the transfer area
         */
        public GlobalTransferArea addTransferSet() {
            String path = createPageArea("transfers", new Runnable() {
                @Override public void run() {
                    add.click();
                }
            });
            return createTransferArea(path);
        }
    }

    /**
     * Describes the TransferArea for publishover
     */
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
