package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import jakarta.inject.Inject;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.By;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;

public class GitLabConnectionConfig extends PageAreaImpl {

    @Inject
    public GitLabConnectionConfig(Jenkins jenkins) {
        super(jenkins, "/com-dabsquared-gitlabjenkins-connection-GitLabConnectionConfig/connections");
    }

    public void configureConnection(String url) {
        find(by.path("/com-dabsquared-gitlabjenkins-connection-GitLabConnectionConfig/connections/name")).sendKeys("aaa");
        find(by.path("/com-dabsquared-gitlabjenkins-connection-GitLabConnectionConfig/connections/url")).sendKeys(url);
        find(by.option("GitLab API token")).click();

        control("validate-button").click();

        waitFor(driver, hasContent("Success"), 2);
    }
}
