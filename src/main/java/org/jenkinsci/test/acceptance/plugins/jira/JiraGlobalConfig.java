package org.jenkinsci.test.acceptance.plugins.jira;

import jakarta.inject.Inject;
import java.net.URL;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * @author Kohsuke Kawaguchi
 */
public class JiraGlobalConfig extends PageAreaImpl {

    @Inject
    public JiraGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/hudson-plugins-jira-JiraGlobalConfiguration");
    }

    // TODO: make this work properly when the site exists already
    public void addSite(URL url, String credentialsId) {
        control("repeatable-add").click();
        control("sites/url").set(url);
        control("sites/credentialsId").select(credentialsId);
    }
}
