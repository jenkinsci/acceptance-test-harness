package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import jakarta.inject.Inject;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.By;

public class GitLabServerConfig extends PageAreaImpl {

    @Inject
    public GitLabServerConfig(Jenkins jenkins) {
        super(jenkins, "/com-dabsquared-gitlabjenkins-connection-GitLabConnectionConfig/connections");
    }

    public String configureServer(String url, String token) {
        control("name").set("servername");
        control("url").set(url);
        control(By.cssSelector("button.credentials-add-menu")).click();
        control(By.cssSelector("li.yuimenuitem")).click();
        find(by.option("GitLab API token")).click();
        find(by.path("/credentials/apiToken")).sendKeys(token);

        find(by.id("credentials-add-submit-button")).click();
        sleep(2000);
        find(by.option("GitLab API token")).click();

        control("validate-button").click();
        sleep(2000);
        return control(By.cssSelector("div.jenkins-validate-button__container__status")).text();
    }
}
