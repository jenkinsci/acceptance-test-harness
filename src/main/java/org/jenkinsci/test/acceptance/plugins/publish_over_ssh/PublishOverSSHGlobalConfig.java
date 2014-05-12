package org.jenkinsci.test.acceptance.plugins.publish_over_ssh;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;

import javax.inject.Inject;

/**
 * @author somebody ;-)
 */
public class PublishOverSSHGlobalConfig extends PageArea {
    @Inject
    JenkinsController controller;

    public final Control add = control("repeatable-add");

    @Inject
    public PublishOverSSHGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/jenkins-plugins-publish_over_ssh-BapSshPublisherPlugin");
    }

    public CommonConfig setCommonConfig() {
        String p = last(by.xpath(".//tr[@name='commonConfig'][starts-with(@path,'/jenkins-plugins-publish_over_ssh-BapSshPublisherPlugin/')]")).getAttribute("path");
        return new CommonConfig(page,p);
    }

    public static class CommonConfig extends PageArea {
        public CommonConfig(PageObject parent, String path) {
            super(parent, path);
        }

        // global controls for all sites
        public final Control encryptedPassphrase = control("encryptedPassphrase");
        public final Control keyPath = control("keyPath");
        public final Control key = control("key");

        public final Control disableAllExecGlobal = control("disableAllExec");

        // Dropdown list for Publisher config class="setting-input dropdownList"
    }

    public InstanceSite addInstanceSite() {
        add.click();
        String p = last(by.xpath(".//div[@name='instance'][starts-with(@path,'/jenkins-plugins-publish_over_ssh-BapSshPublisherPlugin/')]")).getAttribute("path");
        return new InstanceSite(page,p);
    }

    public static class InstanceSite extends PageArea {
        public InstanceSite(PageObject parent, String path) {
            super(parent, path);
        }

        // instance controls
        public final Control name = control("name");
        public final Control hostname = control("hostname");
        public final Control username = control("username");
        public final Control remoteRootDir = control("remoteRootDir");

        // advanced config button
        public final Control addAdvancedConfig = control("advanced-button");

        public AdvancedConfig addAdvancedConfig(){
            addAdvancedConfig.click();
            String p = last(by.xpath(".//div[@name='instance'][starts-with(@path,'/jenkins-plugins-publish_over_ssh-BapSshPublisherPlugin/')]")).getAttribute("path");
            return new AdvancedConfig(page,p);
        }

        public final Control validate = control("validate-button");
        public final Control delete = control("repeatable-delete");
    }

    public static class AdvancedConfig extends PageArea {
        public AdvancedConfig(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control port = control("port");
        public final Control timeout = control("timeout");
        public final Control disableAllExecInstance = control("disableAllExec");
    }

}
