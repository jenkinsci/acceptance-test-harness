package org.jenkinsci.test.acceptance.plugins.jira;

import javax.inject.Inject;
import java.net.URL;

import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * @author Kohsuke Kawaguchi
 */
public class JiraGlobalConfig extends PageAreaImpl {

    @Inject
    public JiraGlobalConfig(Jenkins jenkins) {
        super(jenkins, _getPath(jenkins));
    }

    private static String _getPath(Jenkins jenkins) {
        boolean useNew = jenkins.getPlugin("jira").getVersion().isNewerThan(new VersionNumber("3.0.6"));
        return useNew
                ? "/hudson-plugins-jira-JiraGlobalConfiguration"
                : "/hudson-plugins-jira-JiraProjectProperty"
        ;
    }

    // TODO: make this work properly when the site exists already
    public void addSite(URL url, String credentialsId) {
        control("repeatable-add").click();
        control("sites/url").set(url);
        control( "sites/credentialsId" ).select( credentialsId );
    }
}
