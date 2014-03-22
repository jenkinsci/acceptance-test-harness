package org.jenkinsci.test.acceptance.plugins.scp;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;

import javax.inject.Inject;

/**
 * @author Kohsuke Kawaguchi
 */
public class ScpGlobalConfig extends PageArea {
    @Inject
    JenkinsController controller;

    public final Control add = control("repeatable-add");

    @Inject
    public ScpGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/be-certipost-hudson-plugin-SCPRepositoryPublisher");
    }

    public Site addSite() {
        add.click();
        String p = last(by.xpath(".//div[@name='site'][starts-with(@path,'/be-certipost-hudson-plugin-SCPRepositoryPublisher/')]")).getAttribute("path");
        return new Site(injector,p);
    }

    public static class Site extends PageArea {
        public Site(Injector injector, String path) {
            super(injector, path);
        }

        public final Control hostname = control("hostname");
        public final Control port = control("port");
        public final Control rootRepositoryPath = control("rootRepositoryPath");
        public final Control username = control("username");
        public final Control password = control("password");
        public final Control keyfile = control("keyfile");
    }
}