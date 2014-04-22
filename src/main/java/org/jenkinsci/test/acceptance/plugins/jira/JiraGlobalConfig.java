package org.jenkinsci.test.acceptance.plugins.jira;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageArea;

import javax.inject.Inject;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class JiraGlobalConfig extends PageArea {

    @Inject
    public JiraGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/hudson-plugins-jira-JiraProjectProperty");
    }

    // TODO: make this work properly when the site exists already
    public void addSite(URL url, String user, String password) {
        control("repeatable-add").click();
        control("sites/url").set(url);
        control("sites/userName").set(user);
        control("sites/password").set(password);
    }
}
