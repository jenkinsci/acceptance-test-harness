package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import jakarta.inject.Inject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;

public class GitLabServerConfig extends PageAreaImpl {

    private Control serverName = control("servers/name");
    private Control serverUrl = control("servers/serverUrl");

    @Inject
    public GitLabServerConfig(Jenkins jenkins) {
        super(jenkins, "/io-jenkins-plugins-gitlabserverconfig-servers-GitLabServers");
    }

    public void configureServer(String url) {
        serverName.set("servername");
        serverUrl.set(url);

        waitFor(by.option("GitLab Personal Access Token")).click();

        find(by.path("/io-jenkins-plugins-gitlabserverconfig-servers-GitLabServers/servers/validate-button")).click();

        waitFor(driver, hasContent("Credentials verified for user"), 10);
    }
}
