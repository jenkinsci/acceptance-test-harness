package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import jakarta.inject.Inject;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

public class GitLabServerConfig extends PageAreaImpl {

    @Inject
    public GitLabServerConfig(Jenkins jenkins) {
        super(jenkins, "/io-jenkins-plugins-gitlabserverconfig-servers-GitLabServers");
    }

    public void configureServer(String url) {
        find(by.path("/io-jenkins-plugins-gitlabserverconfig-servers-GitLabServers/servers/name")).sendKeys("servername");
        find(by.path("/io-jenkins-plugins-gitlabserverconfig-servers-GitLabServers/servers/serverUrl")).clear();
        find(by.path("/io-jenkins-plugins-gitlabserverconfig-servers-GitLabServers/servers/serverUrl")).sendKeys(url);

        find(by.option("GitLab Personal Access Token")).click();

        find(by.path("/io-jenkins-plugins-gitlabserverconfig-servers-GitLabServers/servers/validate-button")).click();

        waitFor(driver, hasContent("Credentials verified for user"), 2);
    }
}
