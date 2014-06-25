package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

import javax.inject.Inject;

/**
 * Abstract Class for the Publish over Plugins GLobal Configuration
 * @author Tobias Meyer
 */
public abstract class PublishGlobalConfig extends PageAreaImpl {
    @Inject
    JenkinsController controller;
    /**
     * Add Button for one Server
     */
    public final Control add = control("repeatable-add");

    @Inject
    public PublishGlobalConfig(Jenkins jenkins, String path) {
        super(jenkins, path);
    }

    /**
     * Add one PublishOver ServerConfiguration
     * @return
     */
    public GlobalSite addSite() {
        add.click();
        String p = last(by.xpath(".//div[@name='instance'][starts-with(@path,'"+getPath()+"/')]")).getAttribute("path");
        return new GlobalSite(getPage(), p);
    }

    /**
     * GlobalSite describing the server configuration for  publish over xxx
     */
    public static class GlobalSite extends PageAreaImpl {
        public GlobalSite(PageObject parent, String path) {
            super(parent, path);
            Control advanced = control("advanced-button");
            advanced.click();
        }

        public final Control name = control("name");
        public final Control hostname = control("hostname");
        public final Control username = control("username");
        public final Control password = control("password");
        public final Control port = control("port");
        public final Control timeout = control("timeout");
    }
}