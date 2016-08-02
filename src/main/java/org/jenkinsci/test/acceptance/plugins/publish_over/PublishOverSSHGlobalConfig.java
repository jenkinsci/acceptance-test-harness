package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

import javax.inject.Inject;

/**
 * @author jenky-hm
 */
public class PublishOverSSHGlobalConfig extends PageAreaImpl {

    public final Control add = control("repeatable-add");

    @Inject
    public PublishOverSSHGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/jenkins-plugins-publish_over_ssh-BapSshPublisherPlugin");
    }

    public CommonConfig setCommonConfig() {
        String p = lastIfNotVisible(by.xpath(".//tr[@name='commonConfig'][starts-with(@path,'/jenkins-plugins-publish_over_ssh-BapSshPublisherPlugin/')]")).getAttribute("path");
        return new CommonConfig(getPage(), p);
    }

    public static class CommonConfig extends PageAreaImpl {
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
        String path = createPageArea("instance", new Runnable() {
            @Override public void run() {
                add.click();
            }
        });
        return new InstanceSite(getPage(), path);
    }

    public static class InstanceSite extends PageAreaImpl {
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

        public AdvancedConfig addAdvancedConfig() {
            String path = createPageArea("instance", new Runnable() {
                @Override public void run() {
                    addAdvancedConfig.click();
                }
            });
            return new AdvancedConfig(getPage(), path);
        }

        public final Control validate = control("validate-button");
        public final Control delete = control("repeatable-delete");
    }

    public static class AdvancedConfig extends PageAreaImpl {
        public AdvancedConfig(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control port = control("port");
        public final Control timeout = control("timeout");
        public final Control disableAllExecInstance = control("disableAllExec");
    }

}
